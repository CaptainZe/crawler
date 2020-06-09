package com.ze.crawler.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.IMConstant;
import com.ze.crawler.core.constants.ProxyConstant;
import com.ze.crawler.core.constants.enums.ImSpecialDishEnum;
import com.ze.crawler.core.entity.ImEsports;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.ImEsportsRepository;
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
 * IM电竞盘口
 */
@SuppressWarnings("all")
@Slf4j
@Service
public class ImESportsServiceV1 implements BaseService {
    @Autowired
    private ImEsportsRepository imEsportsRepository;
    @Autowired
    private LogService logService;

    /**
     * IM电竞爬虫
     * @param taskId
     * @param type  赛事类型，LOL、DOTA2、CSGO
     * @param appointedLeagues  指定联赛
     * @param appointedTeams    指定队伍
     */
    @Override
    public void crawler(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        log.info("IM电竞_" + type + "_" + taskId);
        long startTime = System.currentTimeMillis();

        Integer sportId = null;
        if (Constant.ESPORTS_TYPE_LOL.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_LOL;
        } else if (Constant.ESPORTS_TYPE_DOTA2.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_DOTA2;
        } else if (Constant.ESPORTS_TYPE_CSGO.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_CSGO;
        } else if (Constant.ESPORTS_TYPE_KPL.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_KPL;
        }

        if (sportId != null) {
            JSONObject body = getBaseBody(sportId);
            int retryCount = 0;
            while (true) {
                Map<String, Object> map = HttpClientUtils.post(IMConstant.IM_BASE_URL_V1, body, Map.class, ProxyConstant.USE_PROXY);
                if (map != null && map.get("Sport") != null) {
                    List<Map<String, Object>> sports = (List<Map<String, Object>>) map.get("Sport");
                    if (!CollectionUtils.isEmpty(sports)) {
                        try {
                            parseEsports(taskId, type, sports, appointedLeagues, appointedTeams);
                        } catch (Exception e) {
                            Map<String, String> data = new HashMap<>();
                            data.put("url", IMConstant.IM_BASE_URL);
                            data.put("result", JSON.toJSONString(map));
                            data.put("retry_count", String.valueOf(retryCount));
                            logService.log(Constant.LOG_TYPE_PARSE_ESPORTS_ERROR, Constant.ESPORTS_DISH_IM.toString(), JSON.toJSONString(data), e);
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
        log.info("IM电竞_" + type + "_" + taskId + "_[耗时（秒）: " + CommonUtils.getSeconds(endTime - startTime) + "]");
    }

    /**
     * 电竞解析
     * @param taskId
     * @param type
     * @param d
     */
    private void parseEsports(String taskId, String type, List<Map<String, Object>> sports, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        Integer sportId = null;
        if (Constant.ESPORTS_TYPE_LOL.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_LOL;
        } else if (Constant.ESPORTS_TYPE_DOTA2.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_DOTA2;
        } else if (Constant.ESPORTS_TYPE_CSGO.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_CSGO;
        } else if (Constant.ESPORTS_TYPE_KPL.equalsIgnoreCase(type)) {
            sportId = IMConstant.SPORT_ID_KPL;
        }

        if (sportId != null) {
            for (Map<String, Object> sport : sports) {
                Integer returnSportId = (Integer) sport.get("SportId");
                if (returnSportId != sportId) {
                    continue;
                }

                List<Map<String, Object>> leagues = (List<Map<String, Object>>) sport.get("LG");
                if (!CollectionUtils.isEmpty(leagues)) {
                    for (Map<String, Object> league : leagues) {
                        // 联赛名
                        String leagueName = (String) league.get("BaseLGName");
                        leagueName = leagueName.trim();
                        String leagueId = Dictionary.ESPORT_IM_LEAGUE_MAPPING.get(leagueName);
                        if (leagueId == null) {
                            continue;
                        }

                        // 如果存在指定联赛, 进行过滤判断
                        if (!FilterUtils.filterLeague(appointedLeagues, leagueId)) {
                            continue;
                        }

                        List<Map<String, Object>> games = (List<Map<String, Object>>) league.get("ParentMatch");
                        if (!CollectionUtils.isEmpty(games)) {
                            for (Map<String, Object> game : games) {
                                // 状态 -1为未开始
                                Integer gameStatus = (Integer) game.get("GameStatus");
                                if (gameStatus != IMConstant.GAME_STATUS_V1) {
                                    continue;
                                }

                                // 比赛ID, 查询详细盘口需要使用
                                Integer matchId = (Integer) game.get("PMatchNo");
                                String pMatchNo = matchId.toString();

                                // 比赛开始时间
                                String pMCDate = (String) game.get("PMCDate");
                                String startTime = getStartTime(pMCDate);
                                // 只取到第二天的比赛
                                if (TimeUtils.getNextDayLastTime().compareTo(startTime) < 0) {
                                    continue;
                                }

                                // 轮数
                                String round = (String) game.get("MatchType");

                                // 主队
                                String homeTeamName = (String) game.get("PHTName");
                                homeTeamName = homeTeamName.trim();
                                // 客队
                                String guestTeamName = (String) game.get("PATName");
                                guestTeamName = guestTeamName.trim();
                                if (StringUtils.isEmpty(homeTeamName) || StringUtils.isEmpty(guestTeamName)) {
                                    continue;
                                }
                                // 获取主客队信息
                                String homeTeamId = Dictionary.ESPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                                String guestTeamId = Dictionary.ESPORT_IM_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());
                                if (homeTeamId == null || guestTeamId == null) {
                                    continue;
                                }

                                // 如果存在指定队伍, 进行过滤判断
                                if (!FilterUtils.filterTeam(appointedTeams, homeTeamId, guestTeamId)) {
                                    continue;
                                }

                                // 初始化一个, 避免重复赋值
                                ImEsports initImEsports = new ImEsports();
                                initImEsports.setTaskId(taskId);
                                initImEsports.setType(type);
                                initImEsports.setLeagueId(leagueId);
                                initImEsports.setLeagueName(leagueName);
                                initImEsports.setHomeTeamId(homeTeamId);
                                initImEsports.setHomeTeamName(homeTeamName);
                                initImEsports.setGuestTeamId(guestTeamId);
                                initImEsports.setGuestTeamName(guestTeamName);
                                initImEsports.setStartTime(startTime);

                                // 获取详细盘口
                                int retryCount = 0;
                                while (true) {
                                    String url = String.format(IMConstant.IM_MORE_URL_V1, matchId);
                                    JSONObject body = getMoreBody(pMatchNo);
                                    Map<String, Object> map = HttpClientUtils.post(url, body, Map.class, ProxyConstant.USE_PROXY);
                                    if (map != null && map.get("Sport") != null) {
                                        List<Map<String, Object>> moreSports = (List<Map<String, Object>>) map.get("Sport");
                                        if (!CollectionUtils.isEmpty(moreSports)) {
                                            try {
                                                parseMatchDetail(moreSports, initImEsports, round);
                                            } catch (Exception e) {
                                                Map<String, String> data = new HashMap<>();
                                                data.put("url", url);
                                                data.put("result", JSON.toJSONString(map));
                                                data.put("retry_count", String.valueOf(retryCount));
                                                logService.log(Constant.LOG_TYPE_PARSE_ESPORTS_ERROR, Constant.ESPORTS_DISH_IM.toString(), JSON.toJSONString(data), e);
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
                        }
                    }
                }
            }
        }
    }

    /**
     * 解析比赛详细盘口
     */
    private void parseMatchDetail(List<Map<String, Object>> moreSports, ImEsports initImEsports, String round) {
        // 获取对应盘口字典表
        Map<String, String> dishMapping = Dictionary.getEsportDishMappingByTypeAndDishType(initImEsports.getType(), Constant.ESPORTS_DISH_IM);
        // 盘口显示名字典表
        Map<String, String> displayMapping = Dictionary.getImDishDisplayMappingByType(initImEsports.getType());

        // 详细盘口, 就一个
        Map<String, Object> sport = moreSports.get(0);
        List<Map<String, Object>> leagues = (List<Map<String, Object>>) sport.get("LG");
        Map<String, Object> league = leagues.get(0);
        List<Map<String, Object>> parentMatch = (List<Map<String, Object>>) league.get("ParentMatch");
        Map<String, Object> match = parentMatch.get(0);

        // 各个盘口信息
        List<Map<String, Object>> allDish = (List<Map<String, Object>>) match.get("Match");
        if (!CollectionUtils.isEmpty(allDish)) {
            List<ImEsports> imEsportsList = new ArrayList<>();
            for (Map<String, Object> dish : allDish) {
                // 盘口状态，只取1
                Integer status = (Integer) dish.get("Status");
                if (status != IMConstant.DISH_STATUS_V1) {
                    continue;
                }

                // 盘口名
                String dishName = (String) dish.get("GTCode");
                dishName = dishName.trim();
                ImSpecialDishEnum imSpecialDishEnum = ImSpecialDishEnum.getImSpecialDishByOriginalValue(dishName);
                if (imSpecialDishEnum != null) {
                    dishName = imSpecialDishEnum.getCustomValue();
                }

                // 局数
                Integer gameNo = (Integer) dish.get("GameOrder");
                if (!doMap(gameNo, round)) {
                    continue;
                }

                // 匹配盘口映射
                String matchDishName = gameNo + "_" + dishName;
                String dishId = dishMapping.get(matchDishName);
                if (dishId == null) {
                    continue;
                }

                // 各个盘具体处理方式不太一样,只能一个个单独处理
                String dishType = Dictionary.ESPORT_DISH_TYPE_MAPPING.get(dishId);
                if (dishType == null) {
                    continue;
                }

                // 盘口赔率信息，取第一个
                List<Map<String, Object>> odds = (List<Map<String, Object>>) dish.get("Odds");
                if (CollectionUtils.isEmpty(odds) || odds.size() != 1) {
                    continue;
                }
                Map<String, Object> oddsMap = odds.get(0);
                if (CollectionUtils.isEmpty(oddsMap)) {
                    continue;
                }
                List<Map<String, Object>> oddsInfo = (List<Map<String, Object>>) oddsMap.get("SEL");
                if (CollectionUtils.isEmpty(oddsInfo) || oddsInfo.size() != 2) {
                    continue;
                }

                // 主客队赔率信息
                Map<String, Object> homeTeamOddsInfo = getOddsInfo(oddsInfo, IMConstant.HOME_TEAM_ODDS_CODE_V1);
                Map<String, Object> guestTeamOddsInfo = getOddsInfo(oddsInfo, IMConstant.GUEST_TEAM_ODDS_CODE_V1);
                if (CollectionUtils.isEmpty(homeTeamOddsInfo) || CollectionUtils.isEmpty(guestTeamOddsInfo)) {
                    continue;
                }

                // 赋值
                ImEsports imEsports = new ImEsports();
                BeanUtils.copyProperties(initImEsports, imEsports);
                imEsports.setId(LangUtils.generateUuid());
                imEsports.setDishId(dishId);
                imEsports.setDishName(displayMapping.get(matchDishName));

                if (Constant.DISH_TYPE_SYP.equals(dishType)) {
                    BigDecimal homeTeamOdds = (BigDecimal) homeTeamOddsInfo.get("Odds");
                    BigDecimal guestTeamOdds = (BigDecimal) guestTeamOddsInfo.get("Odds");

                    // 输赢盘
                    imEsports.setHomeTeamOdds(homeTeamOdds.toString());
                    imEsports.setGuestTeamOdds(guestTeamOdds.toString());

                    imEsportsList.add(imEsports);
                } else if (Constant.DISH_TYPE_RFP.equals(dishType)) {
                    // 让分盘
                    BigDecimal homeTeamOdds = (BigDecimal) homeTeamOddsInfo.get("Odds");
                    BigDecimal guestTeamOdds = (BigDecimal) guestTeamOddsInfo.get("Odds");

                    String homeTeamItem = null;
                    String guestTeamItem = null;
                    if (imSpecialDishEnum != null) {
                        homeTeamItem = imSpecialDishEnum.getHomeTeamItem();
                        guestTeamItem = imSpecialDishEnum.getGuestTeamItem();
                    } else {
                        BigDecimal rfItemValue = (BigDecimal) homeTeamOddsInfo.get("HDP");
                        String rfItem = rfItemValue.toString();
                        homeTeamItem = "-" + rfItem;
                        guestTeamItem = rfItem;
                    }
                    imEsports.setHomeTeamOdds(homeTeamOdds.toString());
                    imEsports.setHomeTeamItem(homeTeamItem);
                    imEsports.setGuestTeamOdds(guestTeamOdds.toString());
                    imEsports.setGuestTeamItem(guestTeamItem);

                    imEsportsList.add(imEsports);
                } else if (Constant.DISH_TYPE_DXP.equals(dishType) || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                    // 大小盘
                    BigDecimal homeTeamOdds = (BigDecimal) homeTeamOddsInfo.get("Odds");
                    BigDecimal guestTeamOdds = (BigDecimal) guestTeamOddsInfo.get("Odds");

                    BigDecimal dxItemValue = (BigDecimal) homeTeamOddsInfo.get("HDP");
                    String dxItem = dxItemValue.toString();
                    if (!dxItem.contains(".")) {
                        dxItem = dxItem + ".0";
                    }

                    imEsports.setHomeTeamOdds(homeTeamOdds.toString());
                    imEsports.setHomeTeamItem(dxItem);
                    imEsports.setHomeExtraDishName(IMConstant.EXTRA_DISH_NAME_GREATER_THAN);

                    imEsports.setGuestTeamOdds(guestTeamOdds.toString());
                    imEsports.setGuestExtraDishName(IMConstant.EXTRA_DISH_NAME_LESS_THAN);

                    imEsportsList.add(imEsports);
                } else if (Constant.DISH_TYPE_DSP.equals(dishType)) {
                    // 单双盘
                    BigDecimal homeTeamOdds = (BigDecimal) homeTeamOddsInfo.get("Odds");
                    BigDecimal guestTeamOdds = (BigDecimal) guestTeamOddsInfo.get("Odds");

                    imEsports.setHomeTeamOdds(homeTeamOdds.toString());
                    imEsports.setHomeExtraDishName(IMConstant.EXTRA_DISH_NAME_ODD);

                    imEsports.setGuestTeamOdds(guestTeamOdds.toString());
                    imEsports.setGuestExtraDishName(IMConstant.EXTRA_DISH_NAME_EVEN);
                    imEsportsList.add(imEsports);
                } else if (Constant.DISH_TYPE_SFP.equals(dishType)) {
                    // 是否盘
                    // 暂无
                }
            }
            // 保存
            saveImEsports(imEsportsList);
        }
    }

    /**
     * 获取主客队赔率信息
     */
    private Map<String, Object> getOddsInfo(List<Map<String, Object>> oddsInfo, Integer sCode) {
        for (Map<String, Object> odds : oddsInfo) {
            Integer code = (Integer) odds.get("SCode");
            if (code == sCode) {
                return odds;
            }
        }
        return null;
    }

    /**
     * 处理地图数据
     */
    private boolean doMap(Integer gameNo, String round) {
        if (IMConstant.GAME_ROUND_BO5.equalsIgnoreCase(round) || IMConstant.GAME_ROUND_BO7.equalsIgnoreCase(round)) {
            // bo5
            if (gameNo > 3) {
                return false;
            }
        } else if (IMConstant.GAME_ROUND_BO3.equalsIgnoreCase(round)) {
            // bo3
            if (gameNo > 2) {
                return false;
            }
        }

        return true;
    }

    /**
     * 处理比赛开始时间
     * ex:2020-06-09T08:00:00-04:00
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
     * 获取请求body
     */
    private JSONObject getBaseBody(Integer sportId) {
        JSONObject body = new JSONObject();
        body.put("BettingChannel", 1);
        body.put("EventMarket", -99);
        body.put("Language", "chs");
        body.put("Token", null);
        body.put("BaseLGIds", Collections.singletonList(-99));
        body.put("SportId", sportId);
        return body;
    }

    /**
     * 获取请求body
     */
    private JSONObject getMoreBody(String PMatchNo) {
        JSONObject body = new JSONObject();
        body.put("BettingChannel", 1);
        body.put("Language", "chs");
        body.put("Token", null);
        body.put("PMatchNo", PMatchNo);
        return body;
    }

    /**
     * 保存
     * @param imEsports
     */
    private void saveImEsports(List<ImEsports> imEsports) {
        if (!CollectionUtils.isEmpty(imEsports)) {
            imEsportsRepository.saveAll(imEsports);
            imEsportsRepository.flush();
        }
    }
}
