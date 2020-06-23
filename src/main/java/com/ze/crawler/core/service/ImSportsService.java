package com.ze.crawler.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.IMConstant;
import com.ze.crawler.core.constants.ProxyConstant;
import com.ze.crawler.core.entity.ImSports;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.ImSportsRepository;
import com.ze.crawler.core.service.log.LogService;
import com.ze.crawler.core.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * IM盘口 - 体育
 */
@SuppressWarnings("all")
@Slf4j
@Service
public class ImSportsService implements BaseService {
    @Autowired
    private ImSportsRepository imSportsRepository;
    @Autowired
    private LogService logService;

    @Override
    public void crawler(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        log.info("IM体育_" + type + "_" + taskId);
        long startTime = System.currentTimeMillis();

        Integer sportId = null;
        if (Constant.SPORTS_TYPE_SOCCER.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_SOCCER;
        } else if (Constant.SPORTS_TYPE_BASKETBALL.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_BASKETBALL;
        }

        if (sportId != null) {
            // 今日
            int retryCount = 0;
            while (true) {
                JSONObject todayBody = getBaseBody(sportId, IMConstant.MARKET_TODAY, null, null);
                Map<String, Object> map = HttpClientUtils.post(IMConstant.IM_SPORT_BASE_URL, todayBody, Map.class, ProxyConstant.USE_PROXY);
                if (map != null && map.get("sel") != null) {
                    List<Map<String, Object>> sels = (List<Map<String, Object>>) map.get("sel");
                    if (!CollectionUtils.isEmpty(sels)) {
                        try {
                            parseSports(taskId, type, sels, appointedLeagues, appointedTeams);
                        } catch (Exception e) {
                            Map<String, String> data = new HashMap<>();
                            data.put("url", IMConstant.IM_BASE_URL);
                            data.put("result", JSON.toJSONString(map));
                            data.put("retry_count", String.valueOf(retryCount));
                            logService.log(Constant.LOG_TYPE_PARSE_SPORTS_ERROR, Constant.SPORTS_DISH_IM.toString(), JSON.toJSONString(data), e);
                        }
                        break;
                    }
                }

                retryCount++;
                if (retryCount >= Constant.RETRY_COUNT) {
                    break;
                }
            }

            // 早盘
            retryCount = 0;
            while (true) {
                String zpDate = TimeUtils.getNextDay(TimeUtils.TIME_FORMAT_3);
                JSONObject zpBody = getBaseBody(sportId, IMConstant.MARKET_ZP, zpDate, zpDate);
                Map<String, Object> map = HttpClientUtils.post(IMConstant.IM_SPORT_BASE_URL, zpBody, Map.class, ProxyConstant.USE_PROXY);
                if (map != null && map.get("sel") != null) {
                    List<Map<String, Object>> sels = (List<Map<String, Object>>) map.get("sel");
                    if (!CollectionUtils.isEmpty(sels)) {
                        try {
                            parseSports(taskId, type, sels, appointedLeagues, appointedTeams);
                        } catch (Exception e) {
                            Map<String, String> data = new HashMap<>();
                            data.put("url", IMConstant.IM_BASE_URL);
                            data.put("result", JSON.toJSONString(map));
                            data.put("retry_count", String.valueOf(retryCount));
                            logService.log(Constant.LOG_TYPE_PARSE_SPORTS_ERROR, Constant.SPORTS_DISH_IM.toString(), JSON.toJSONString(data), e);
                        }
                        break;
                    }
                }

                retryCount++;
                if (retryCount >= Constant.RETRY_COUNT) {
                    break;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("IM体育_" + type + "_" + taskId + "_[耗时（秒）: " + CommonUtils.getSeconds(endTime - startTime) + "]");
    }

    /**
     * 体育解析
     */
    private void parseSports(String taskId, String type, List<Map<String, Object>> sels, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        if (!CollectionUtils.isEmpty(sels)) {
            // 遍历比赛
            for (Map<String, Object> sel : sels) {
                // 联赛名
                String leagueName = ((String) sel.get("cn")).trim();

                // 忽略电竞足球
                if (leagueName.startsWith(IMConstant.LEAGUE_NAME_IGNORE_DZZQ)) {
                    continue;
                }

                // 赛事信息获取
                String leagueId = Dictionary.SPORT_IM_LEAGUE_MAPPING.get(type).get(leagueName);
                if (leagueId == null) {
                    continue;
                }

                // 如果存在指定联赛, 进行过滤判断
                if (!FilterUtils.filterLeague(appointedLeagues, leagueId)) {
                    continue;
                }

                // 队伍名
                String homeTeamName = ((String) sel.get("htn")).trim();
                String guestTeamName = ((String) sel.get("atn")).trim();
                if (StringUtils.isEmpty(homeTeamName) || StringUtils.isEmpty(guestTeamName)) {
                    continue;
                }

                // 获取主客队信息
                String homeTeamId = Dictionary.SPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                String guestTeamId = Dictionary.SPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());
                if (homeTeamId == null || guestTeamId == null) {
                    continue;
                }

                // 如果存在指定队伍, 进行过滤判断
                if (!FilterUtils.filterTeam(appointedTeams, homeTeamId, guestTeamId)) {
                    continue;
                }

                // 比赛开始时间
                String startTime = (String) sel.get("edt");
                startTime = getStartTime(startTime);
                if (StringUtils.isEmpty(startTime)) {
                    continue;
                }

                // 初始化一个
                ImSports initImSports = new ImSports();
                initImSports.setTaskId(taskId);
                initImSports.setType(type);
                initImSports.setLeagueId(leagueId);
                initImSports.setLeagueName(leagueName);
                initImSports.setHomeTeamId(homeTeamId);
                initImSports.setHomeTeamName(homeTeamName);
                initImSports.setGuestTeamId(guestTeamId);
                initImSports.setGuestTeamName(guestTeamName);
                initImSports.setStartTime(startTime);

                // 获取对应盘口字典表
                Map<String, String> dishMapping = Dictionary.SPORT_IM_DISH_MAPPING.get(type);

                // 具体盘口数据
                List<ImSports> imSportsList = new ArrayList<>();
                List<Map<String, Object>> mls = (List<Map<String, Object>>) sel.get("mls");
                if (!CollectionUtils.isEmpty(mls)) {
                    for (Map<String, Object> ml : mls) {
                        Integer bti = (Integer) ml.get("bti");
                        Integer pi = (Integer) ml.get("pi");
                        List<Map<String, Object>> ws = (List<Map<String, Object>>) ml.get("ws");
                        if (bti != null && pi != null && !CollectionUtils.isEmpty(ws)) {
                            ImSports imSports = null;
                            if (IMConstant.BTI_RFP.equals(bti)) {
                                // 让分盘
                                imSports = dishHandler4Rfp(initImSports, pi, ws, dishMapping);
                            } else if (IMConstant.BTI_DXP.equals(bti)) {
                                // 大小盘
                                imSports = dishHandler4Dxp(initImSports, pi, ws, dishMapping);
                            } else if (IMConstant.BTI_SYP.equals(bti) && Constant.SPORTS_TYPE_BASKETBALL.equalsIgnoreCase(type)) {
                                // 输赢盘 - 只有篮球需要
                                imSports = dishHandler4Syp(initImSports, pi, ws, dishMapping);
                            }

                            if (imSports != null) {
                                imSportsList.add(imSports);
                            }
                        }
                    }
                }
                // 保存
                saveImSports(imSportsList);
            }
        }
    }

    /**
     * 盘口处理方式 - 让分盘
     */
    private ImSports dishHandler4Rfp(ImSports initImSports, Integer pi, List<Map<String, Object>> ws, Map<String, String> dishMapping) {
        String dishName = null;
        if (pi.equals(IMConstant.PI_FULL)) {
            dishName = IMConstant.CUSTOM_DISH_NAME_FULL_RFP;
        } else if (pi.equals(IMConstant.PI_FIRST_HALF)) {
            dishName = IMConstant.CUSTOM_DISH_NAME_FIRST_HALF_RFP;
        }
        if (dishName != null) {
            String dishId = dishMapping.get(dishName);
            if (dishId != null) {
                ImSports imSports = new ImSports();
                BeanUtils.copyProperties(initImSports, imSports);
                imSports.setId(LangUtils.generateUuid());
                imSports.setDishId(dishId);
                imSports.setDishName(dishName);

                int i = 0;
                for (Map<String, Object> w : ws) {
                    BigDecimal hdp = (BigDecimal) w.get("hdp");
                    BigDecimal o = (BigDecimal) w.get("o");

                    if (0 == i) {
                        // 主队赔率
                        imSports.setHomeTeamOdds(o.toString());
                        imSports.setHomeTeamItem(hdp.negate().toString());
                    } else {
                        // 客队赔率
                        imSports.setGuestTeamOdds(o.toString());
                        imSports.setGuestTeamItem(hdp.toString());
                    }

                    i++;
                }

                return imSports;
            }
        }
        return null;
    }

    /**
     * 盘口处理方式 - 大小盘
     */
    private ImSports dishHandler4Dxp(ImSports initImSports, Integer pi, List<Map<String, Object>> ws, Map<String, String> dishMapping) {
        String dishName = null;
        if (pi.equals(IMConstant.PI_FULL)) {
            dishName = IMConstant.CUSTOM_DISH_NAME_FULL_DXP;
        } else if (pi.equals(IMConstant.PI_FIRST_HALF)) {
            dishName = IMConstant.CUSTOM_DISH_NAME_FIRST_HALF_DXP;
        }
        if (dishName != null) {
            String dishId = dishMapping.get(dishName);
            if (dishId != null) {
                ImSports imSports = new ImSports();
                BeanUtils.copyProperties(initImSports, imSports);
                imSports.setId(LangUtils.generateUuid());
                imSports.setDishId(dishId);
                imSports.setDishName(dishName);

                int i = 0;
                for (Map<String, Object> w : ws) {
                    BigDecimal hdp = (BigDecimal) w.get("hdp");
                    BigDecimal o = (BigDecimal) w.get("o");

                    if (0 == i) {
                        // 主队赔率
                        imSports.setHomeTeamOdds(o.toString());
                        imSports.setHomeTeamItem(hdp.toString());
                        imSports.setHomeExtraDishName(IMConstant.EXTRA_DISH_NAME_GREATER_THAN);
                    } else {
                        // 客队赔率
                        imSports.setGuestTeamOdds(o.toString());
                        imSports.setGuestExtraDishName(IMConstant.EXTRA_DISH_NAME_LESS_THAN);
                    }

                    i++;
                }

                return imSports;
            }
        }
        return null;
    }

    /**
     * 盘口处理方式 - 输赢盘
     */
    private ImSports dishHandler4Syp(ImSports initImSports, Integer pi, List<Map<String, Object>> ws, Map<String, String> dishMapping) {
        String dishName = null;
        if (pi.equals(IMConstant.PI_FULL)) {
            dishName = IMConstant.CUSTOM_DISH_NAME_FULL_SYP;
        } else if (pi.equals(IMConstant.PI_FIRST_HALF)) {
            dishName = IMConstant.CUSTOM_DISH_NAME_FIRST_HALF_SYP;
        }
        if (dishName != null) {
            String dishId = dishMapping.get(dishName);
            if (dishId != null) {
                ImSports imSports = new ImSports();
                BeanUtils.copyProperties(initImSports, imSports);
                imSports.setId(LangUtils.generateUuid());
                imSports.setDishId(dishId);
                imSports.setDishName(dishName);

                int i = 0;
                for (Map<String, Object> w : ws) {
                    BigDecimal o = (BigDecimal) w.get("o");

                    if (0 == i) {
                        // 主队赔率
                        imSports.setHomeTeamOdds(o.toString());
                    } else {
                        // 客队赔率
                        imSports.setGuestTeamOdds(o.toString());
                    }

                    i++;
                }

                return imSports;
            }
        }
        return null;
    }

    /**
     * 处理比赛开始时间
     */
    private String getStartTime(String startTimeStr) {
        startTimeStr = startTimeStr.substring(0, startTimeStr.lastIndexOf("-"));
        startTimeStr = startTimeStr.replace("T", " ");

        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.TIME_FORMAT_2);
        try {
            Date startDate = sdf.parse(startTimeStr);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            calendar.setTime(startDate);

            // 加12小时
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 12);
            return TimeUtils.format(calendar.getTime().getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取请求Body
     */
    private JSONObject getBaseBody(Integer sportId, Integer market, String dateFrom, String dateTo) {
        JSONObject body = new JSONObject();
        body.put("BetTypeIds", new Integer[] {1, 2, 4});
        body.put("DateFrom", null);
        body.put("DateTo", null);
        if (!StringUtils.isEmpty(dateFrom) && !StringUtils.isEmpty(dateTo)) {
            body.put("DateFrom", dateFrom);
            body.put("DateTo", dateTo);
        }
        body.put("IsCombo", false);
        body.put("Market", market);
        body.put("MatchDay", 0);
        body.put("OddsType", 3);
        body.put("PeriodIds", new Integer[] {1, 2});
        body.put("Season", 0);
        body.put("SortType", 1);
        body.put("SportId", sportId);
        return body;
    }

    /**
     * 保存
     * @param imSports
     */
    private void saveImSports(List<ImSports> imSports) {
        if (!CollectionUtils.isEmpty(imSports)) {
            imSportsRepository.saveAll(imSports);
            imSportsRepository.flush();
        }
    }
}
