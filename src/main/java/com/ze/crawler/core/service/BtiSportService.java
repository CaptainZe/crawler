package com.ze.crawler.core.service;

import com.alibaba.fastjson.JSON;
import com.ze.crawler.core.constants.BTIConstant;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.PBConstant;
import com.ze.crawler.core.entity.BtiSports;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.BtiSportsRepository;
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

@SuppressWarnings("all")
@Slf4j
@Service
public class BtiSportService implements BaseService {
    @Autowired
    private BtiSportsRepository btiSportsRepository;
    @Autowired
    private LogService logService;

    @Override
    public void crawler(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        log.info("BTI体育_" + type + "_" + taskId);
        long startTime = System.currentTimeMillis();

        Integer branchId = null;
        if (Constant.SPORTS_TYPE_SOCCER.equalsIgnoreCase(type)) {
            branchId = BTIConstant.BRANCH_ID_SOCCER;
        } else if (Constant.SPORTS_TYPE_BASKETBALL.equalsIgnoreCase(type)) {
            branchId = BTIConstant.BRANCH_ID_BASKETBALL;
        }

        if (branchId != null) {
            // 今日
            int retryCount = 0;
            while (true) {
                String url = String.format(BTIConstant.BTI_TODAY_URL, branchId);
                List list = HttpClientUtils.get(url, List.class, getRequestHeaders(), null, false);
                if (list != null) {
                    try {
                        parseSports(taskId, type, list, appointedLeagues, appointedTeams);
                    } catch (Exception e) {
                        Map<String, String> data = new HashMap<>();
                        data.put("url", url);
                        data.put("result", JSON.toJSONString(list));
                        data.put("retry_count", String.valueOf(retryCount));
                        logService.log(Constant.LOG_TYPE_PARSE_SPORTS_ERROR, Constant.SPORTS_DISH_BTI.toString(), JSON.toJSONString(data), e);
                    }
                    break;
                }

                retryCount++;
                if (retryCount >= Constant.RETRY_COUNT) {
                    break;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("BTI体育_" + type + "_" + taskId + "_[耗时（秒）: " + CommonUtils.getSeconds(endTime - startTime) + "]");
    }

    /**
     * 体育解析
     */
    private void parseSports(String taskId, String type, List list, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        // 【1】是联赛列表
        List<List<Object>> leagueList = (List<List<Object>>) list.get(1);
        if (!CollectionUtils.isEmpty(leagueList)) {
            // 联赛信息
            Map<Integer, String> leagueMap = new HashMap<>();
            for (List<Object> leagueInfo : leagueList) {
                if (!CollectionUtils.isEmpty(leagueInfo)) {
                    Integer leagueId = (Integer) leagueInfo.get(0);
                    String leagueName = (String) leagueInfo.get(1);
                    leagueName = leagueName.trim();

                    if (leagueName.startsWith(BTIConstant.LEAGUE_NAME_IGNORE_FIFA)) {
                        continue;
                    }

                    if (!Dictionary.SPORT_BTI_LEAGUE_MAPPING.get(type).containsKey(leagueName)) {
                        continue;
                    }

                    leagueMap.put(leagueId, leagueName);
                }
            }

            if (!CollectionUtils.isEmpty(leagueMap)) {
                // 【2】是比赛列表
                List<List<Object>> gameList = (List<List<Object>>) list.get(2);
                if (!CollectionUtils.isEmpty(gameList)) {
                    // 比赛Map key:gameId value:BtiSport
                    Map<Integer, BtiSports> gameMap = new LinkedHashMap<>();
                    // 盘口ID列表
                    List<Integer> oddsIdList = new ArrayList<>();
                    // 比赛和盘口映射 key:oddsId value: gameId
                    Map<Integer, Integer> gameOddsMap = new HashMap<>();

                    for (List<Object> gameInfo : gameList) {
                        // 【14】联赛ID（盘口内的）
                        Integer dishLeagueId = (Integer) gameInfo.get(14);
                        if (!leagueMap.containsKey(dishLeagueId)) {
                            continue;
                        }
                        String leagueName = leagueMap.get(dishLeagueId);

                        // 赛事信息获取
                        String leagueId = Dictionary.SPORT_BTI_LEAGUE_MAPPING.get(type).get(leagueName);
                        if (leagueId == null) {
                            continue;
                        }

                        // 如果存在指定联赛, 进行过滤判断
                        if (!FilterUtils.filterLeague(appointedLeagues, leagueId)) {
                            continue;
                        }

                        // 【5】盘口集合
                        List<List<Object>> oddsInfoList = (List<List<Object>>) gameInfo.get(5);
                        if (CollectionUtils.isEmpty(oddsInfoList)) {
                            continue;
                        }

                        // 【4】比赛开始时间
                        String startTime = (String) gameInfo.get(4);
                        startTime = getStartTime(startTime);
                        if (StringUtils.isEmpty(startTime)) {
                            continue;
                        }
                        // 【0】比赛ID
                        Integer gameId = (Integer) gameInfo.get(0);
                        // 【1】主队名
                        String homeTeamName = (String) gameInfo.get(1);
                        homeTeamName = homeTeamName.trim();
                        // 【2】客队名
                        String guestTeamName = (String) gameInfo.get(2);
                        guestTeamName = guestTeamName.trim();

                        if (StringUtils.isEmpty(homeTeamName) || StringUtils.isEmpty(guestTeamName)) {
                            continue;
                        }

                        // 获取主客队信息
                        String homeTeamId = Dictionary.SPORT_BTI_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                        String guestTeamId = Dictionary.SPORT_BTI_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());
                        if (homeTeamId == null || guestTeamId == null) {
                            continue;
                        }

                        // 如果存在指定队伍, 进行过滤判断
                        if (!FilterUtils.filterTeam(appointedTeams, homeTeamId, guestTeamId)) {
                            continue;
                        }

                        // 初始化一个
                        BtiSports btiSports = new BtiSports();
                        btiSports.setTaskId(taskId);
                        btiSports.setType(type);
                        btiSports.setLeagueId(leagueId);
                        btiSports.setLeagueName(leagueName);
                        btiSports.setHomeTeamId(homeTeamId);
                        btiSports.setHomeTeamName(homeTeamName);
                        btiSports.setGuestTeamId(guestTeamId);
                        btiSports.setGuestTeamName(guestTeamName);
                        btiSports.setStartTime(startTime);

                        // 获取比赛信息
                        gameMap.put(gameId, btiSports);

                        // 获取请求具体赔率的body
                        for (List<Object> oddsInfo : oddsInfoList) {
                            Integer oddsId = (Integer) oddsInfo.get(0);
                            Integer oddsType = (Integer) oddsInfo.get(1);

                            if (BTIConstant.ODDS_TYPE_0.equals(oddsType)) {
                                oddsIdList.add(oddsId);
                                gameOddsMap.put(oddsId, gameId);
                            }
                        }
                    }

                    if (CollectionUtils.isEmpty(gameMap) || CollectionUtils.isEmpty(oddsIdList)) {
                        return;
                    }

                    // 请求具体赔率
                    int retryCount = 0;
                    while (true) {
                        List oddsList = HttpClientUtils.postFrom(BTIConstant.BTI_ODDS_URL, getRequestParams(oddsIdList), getRequestHeaders(), List.class,false);
                        if (oddsList != null) {
                            try {
                                // 获取对应盘口字典表
                                Map<String, String> dishMapping = Dictionary.SPORT_BTI_DISH_MAPPING.get(type);

                                List<BtiSports> btiSportsList = new ArrayList<>();
                                if (!CollectionUtils.isEmpty(oddsIdList)) {
                                    for (Object oddsInfo : oddsIdList) {
                                        List<Object> oddsInfoList = (List<Object>) oddsInfo;
                                        // 判断是否是全场
                                        Integer fullName = oddsIdList.get(6);
                                        if (!BTIConstant.FULL_NAME.equals(fullName)) {
                                            continue;
                                        }
                                        // 盘口Id
                                        Integer oddsId = (Integer) oddsInfoList.get(0);
                                        // 具体盘口数据
                                        List<List<Object>> oddsDetails = (List<List<Object>>) oddsInfoList.get(2);
                                        if (CollectionUtils.isEmpty(oddsDetails)) {
                                            continue;
                                        }
                                        // 获取比赛ID
                                        if (!gameOddsMap.containsKey(oddsId)) {
                                            continue;
                                        }
                                        Integer gameId = gameOddsMap.get(oddsId);
                                        // 获取初始化实体
                                        if (!gameMap.containsKey(gameId)) {
                                            continue;
                                        }
                                        BtiSports initBtiSports = gameMap.get(gameId);

                                        for (List<Object> oddsDetail : oddsDetails) {
                                            // [2] 让分盘
                                            List<Object> rfpInfo = (List<Object>) oddsDetail.get(2);
                                            if (!CollectionUtils.isEmpty(rfpInfo)) {
                                                // 主队让分
                                                BigDecimal homeTeamItem = (BigDecimal) rfpInfo.get(1);
                                                BigDecimal guestTeamItem = homeTeamItem.negate();

                                                // 主客队赔率
                                                Integer homeTeamOdds = (Integer) rfpInfo.get(2);
                                                Integer guestTeamOdds = (Integer) rfpInfo.get(4);

                                                BtiSports btiSports = new BtiSports();
                                                BeanUtils.copyProperties(initBtiSports, btiSports);
                                                btiSports.setId(LangUtils.generateUuid());
                                                btiSports.setDishId(dishMapping.get(BTIConstant.CUSTOM_DISH_NAME_FULL_RFP));
                                                btiSports.setDishName(BTIConstant.CUSTOM_DISH_NAME_FULL_RFP);
                                                btiSports.setHomeTeamOdds(getOdds(homeTeamOdds));
                                                btiSports.setGuestTeamOdds(getOdds(guestTeamOdds));
                                                btiSports.setHomeTeamItem(homeTeamItem.toString());
                                                btiSports.setGuestTeamItem(guestTeamItem.toString());
                                                btiSportsList.add(btiSports);
                                            }

                                            // [3] 大小盘
                                            List<Object> dxpInfo = (List<Object>) oddsDetail.get(3);
                                            if (!CollectionUtils.isEmpty(dxpInfo)) {
                                                // 大小数
                                                BigDecimal dxItem = (BigDecimal) rfpInfo.get(1);

                                                // 主客队赔率
                                                Integer homeTeamOdds = (Integer) rfpInfo.get(2);
                                                Integer guestTeamOdds = (Integer) rfpInfo.get(4);

                                                BtiSports btiSports = new BtiSports();
                                                BeanUtils.copyProperties(initBtiSports, btiSports);
                                                btiSports.setId(LangUtils.generateUuid());
                                                btiSports.setDishId(dishMapping.get(BTIConstant.CUSTOM_DISH_NAME_FULL_RFP));
                                                btiSports.setDishName(BTIConstant.CUSTOM_DISH_NAME_FULL_RFP);
                                                btiSports.setHomeTeamOdds(getOdds(homeTeamOdds));
                                                btiSports.setGuestTeamOdds(getOdds(guestTeamOdds));
                                                btiSports.setHomeTeamItem(dxItem.toString());
                                                btiSports.setHomeExtraDishName(PBConstant.EXTRA_DISH_NAME_DP);
                                                btiSports.setGuestExtraDishName(PBConstant.EXTRA_DISH_NAME_XP);
                                                btiSportsList.add(btiSports);
                                            }
                                        }
                                    }
                                }
                                saveBtiSports(btiSportsList);

                            } catch (Exception e) {
                                Map<String, String> data = new HashMap<>();
                                data.put("url", BTIConstant.BTI_ODDS_URL);
                                data.put("result", JSON.toJSONString(oddsList));
                                data.put("retry_count", String.valueOf(retryCount));
                                logService.log(Constant.LOG_TYPE_PARSE_SPORTS_ERROR, Constant.SPORTS_DISH_BTI.toString(), JSON.toJSONString(data), e);
                            }
                            break;
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

    /**
     * 处理比赛开始时间
     */
    private String getStartTime(String startTimeStr) {
        startTimeStr = startTimeStr.substring(0, startTimeStr.lastIndexOf("."));
        startTimeStr = startTimeStr.replace("T", " ");

        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.TIME_FORMAT_2);
        try {
            Date startDate = sdf.parse(startTimeStr);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            calendar.setTime(startDate);

            // 加8小时
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 8);
            return TimeUtils.format(calendar.getTime().getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取请求头
     * @return
     */
    private Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("RequestTarget", "AJAXService");
        return headers;
    }

    /**
     * 获取请求body
     * @param oddsIdList
     * @return
     */
    private Map<String, Object> getRequestParams(List<Integer> oddsIdList) {
        Map<String, Object> params = new HashMap<>();
        params.put("requestString", org.apache.commons.lang3.StringUtils.join(oddsIdList, "@"));
        return params;
    }

    /**
     * 计算欧洲盘赔率
     * @param odds
     * @return
     */
    public static String getOdds(Integer odds) {
        double d = odds / 1.0;
        if (odds < 0) {
            d = Math.floor(-10000.0/odds);
        }
        BigDecimal bigDecimal = new BigDecimal(d);
        bigDecimal = bigDecimal.add(new BigDecimal(100));
        bigDecimal = bigDecimal.divide(new BigDecimal("100.0"), 2, BigDecimal.ROUND_DOWN);
        return bigDecimal.toString();
    }

    /**
     * 保存
     * @param btiSports
     */
    private void saveBtiSports(List<BtiSports> btiSports) {
        if (!CollectionUtils.isEmpty(btiSports)) {
            btiSportsRepository.saveAll(btiSports);
            btiSportsRepository.flush();
        }
    }
}
