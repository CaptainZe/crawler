package com.ze.crawler.core.service;

import com.alibaba.fastjson.JSON;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.YBBConstant;
import com.ze.crawler.core.entity.YbbSports;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.YbbSportsRepository;
import com.ze.crawler.core.service.log.LogService;
import com.ze.crawler.core.utils.CommonUtils;
import com.ze.crawler.core.utils.FilterUtils;
import com.ze.crawler.core.utils.HttpClientUtils;
import com.ze.crawler.core.utils.LangUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 188盘口 - 体育
 */
@SuppressWarnings("all")
@Slf4j
@Service
public class YbbSportsService implements BaseService {
    @Autowired
    private YbbSportsRepository ybbSportsRepository;
    @Autowired
    private LogService logService;

    @Override
    public void crawler(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        log.info("188体育_" + type + "_" + taskId);
        long startTime = System.currentTimeMillis();

        // 今日
        int retryCount = 0;
        while (true) {
            String url = String.format(YBBConstant.YBB_BASE_URL, System.currentTimeMillis());
            Map<String, Object> map = HttpClientUtils.postFrom(url, getFormData(true), Map.class);
            if (map != null && map.get("mod") != null) {
                try {
                    parseSports(taskId, type, map, appointedLeagues, appointedTeams);
                } catch (Exception e) {
                    Map<String, String> data = new HashMap<>();
                    data.put("url", url);
                    data.put("result", JSON.toJSONString(map));
                    data.put("retry_count", String.valueOf(retryCount));
                    logService.log(Constant.LOG_TYPE_PARSE_SPORTS_ERROR, Constant.SPORTS_DISH_YB.toString(), JSON.toJSONString(data), e);
                }
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                break;
            }
        }

        // 明日
        retryCount = 0;
        while (true) {
            String url = String.format(YBBConstant.YBB_BASE_URL, System.currentTimeMillis());
            Map<String, Object> map = HttpClientUtils.postFrom(url, getFormData(false), Map.class);
            if (map != null && map.get("mod") != null) {
                try {
                    parseSports(taskId, type, map, appointedLeagues, appointedTeams);
                } catch (Exception e) {
                    Map<String, String> data = new HashMap<>();
                    data.put("url", url);
                    data.put("result", JSON.toJSONString(map));
                    data.put("retry_count", String.valueOf(retryCount));
                    logService.log(Constant.LOG_TYPE_PARSE_SPORTS_ERROR, Constant.SPORTS_DISH_YB.toString(), JSON.toJSONString(data), e);
                }
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("188体育_" + type + "_" + taskId + "_[耗时（秒）: " + CommonUtils.getSeconds(endTime - startTime) + "]");
    }

    /**
     * 体育解析
     */
    private void parseSports(String taskId, String type, Map<String, Object> map, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        Map<String, Object> mod = (Map<String, Object>) map.get("mod");
        if (!CollectionUtils.isEmpty(mod)) {
            List<Map<String, Object>> d = (List<Map<String, Object>>) mod.get("d");
            if (!CollectionUtils.isEmpty(d)) {
                Map<String, Object> cMap = d.get(0);
                if (!CollectionUtils.isEmpty(cMap)) {
                    // 联赛列表
                    List<Map<String, Object>> leagues = (List<Map<String, Object>>) cMap.get("c");
                    if (!CollectionUtils.isEmpty(leagues)) {
                        for (Map<String, Object> league : leagues) {
                            // 联赛名
                            String leagueName = (String) league.get("n");

                            // 赛事信息获取
                            String leagueId = Dictionary.SPORT_YB_LEAGUE_MAPPING.get(leagueName);
                            if (leagueId == null) {
                                continue;
                            }

                            // 如果存在指定联赛, 进行过滤判断
                            if (!FilterUtils.filterLeague(appointedLeagues, leagueId)) {
                                continue;
                            }

                            List<YbbSports> ybbSportsList = new ArrayList<>();
                            // 具体比赛列表
                            List<Map<String, Object>> games = (List<Map<String, Object>>) league.get("e");
                            if (!CollectionUtils.isEmpty(games)) {
                                for (Map<String, Object> game : games) {
                                    // 判断是不是角球/罚牌数等，需要跳过
                                    Map<String, Object> cei = (Map<String, Object>) game.get("cei");
                                    String n = (String) cei.get("n");
                                    if (!"".equals(n)) {
                                        continue;
                                    }

                                    // 队伍信息
                                    List<String> teamInfo = (List<String>) game.get("i");
                                    if (CollectionUtils.isEmpty(teamInfo) || teamInfo.size() < 2) {
                                        continue;
                                    }
                                    // home team name
                                    String homeTeamName = (String) teamInfo.get(0);
                                    // guest team name
                                    String guestTeamName = (String) teamInfo.get(1);
                                    if (StringUtils.isEmpty(homeTeamName) || StringUtils.isEmpty(guestTeamName)) {
                                        continue;
                                    }

                                    // 获取主客队信息
                                    String homeTeamId = Dictionary.SPORT_YB_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName);
                                    String guestTeamId = Dictionary.SPORT_YB_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName);
                                    if (homeTeamId == null || guestTeamId == null) {
                                        continue;
                                    }

                                    // 如果存在指定队伍, 进行过滤判断
                                    if (!FilterUtils.filterTeam(appointedTeams, homeTeamId, guestTeamId)) {
                                        continue;
                                    }

                                    // 开赛时间
                                    String startTime = (String) game.get("edt");
                                    startTime = getStartTime(startTime);

                                    // 初始化一个, 避免重复赋值
                                    YbbSports initYbbSports = new YbbSports();
                                    initYbbSports.setTaskId(taskId);
                                    initYbbSports.setType(type);
                                    initYbbSports.setLeagueId(leagueId);
                                    initYbbSports.setLeagueName(leagueName);
                                    initYbbSports.setHomeTeamId(homeTeamId);
                                    initYbbSports.setHomeTeamName(homeTeamName);
                                    initYbbSports.setGuestTeamId(guestTeamId);
                                    initYbbSports.setGuestTeamName(guestTeamName);
                                    initYbbSports.setStartTime(startTime);

                                    // 获取对应盘口字典表
                                    Map<String, String> dishMapping = Dictionary.getSportDishMappingByTypeAndDishType(type, Constant.SPORTS_DISH_YB);

                                    // 具体赔率
                                    Map<String, List<String>> oddsInfo = (Map<String, List<String>>) game.get("o");
                                    if (!CollectionUtils.isEmpty(oddsInfo)) {
                                        for (String oddsType : oddsInfo.keySet()) {
                                            List<YbbSports> tempList = new ArrayList<>();
                                            if (YBBConstant.ODDS_TYPE_RFP.equalsIgnoreCase(oddsType)
                                                    || YBBConstant.ODDS_TYPE_RFP_1ST.equalsIgnoreCase(oddsType)) {
                                                // 让分盘
                                                List<String> odds = oddsInfo.get(oddsType);
                                                if (CollectionUtils.isEmpty(odds)) {
                                                    continue;
                                                }
                                                tempList = dishHandler4Rfp(initYbbSports, oddsType, odds, dishMapping);
                                            } else if (YBBConstant.ODDS_TYPE_DXP.equalsIgnoreCase(oddsType)
                                                    || YBBConstant.ODDS_TYPE_DXP_1ST.equalsIgnoreCase(oddsType)) {
                                                // 大小盘
                                                List<String> odds = oddsInfo.get(oddsType);
                                                if (CollectionUtils.isEmpty(odds)) {
                                                    continue;
                                                }
                                                tempList = dishHandler4Dxp(initYbbSports, oddsType, odds, dishMapping);
                                            } else {
                                                continue;
                                            }

                                            if (!CollectionUtils.isEmpty(tempList)) {
                                                ybbSportsList.addAll(tempList);
                                            }
                                        }
                                    }
                                }
                            }
                            // 保存
                            saveYbbSports(ybbSportsList);
                        }
                    }
                }
            }
        }
    }

    /**
     * 盘口处理方式 - 让分盘
     */
    private List<YbbSports> dishHandler4Rfp(YbbSports initYbbSports, String oddsType, List<String> odds, Map<String, String> dishMapping) {
        List<YbbSports> ybbSportsList = new ArrayList<>();

        String dishName = null;
        if (YBBConstant.ODDS_TYPE_RFP.equalsIgnoreCase(oddsType)) {
            dishName = YBBConstant.CUSTOM_DISH_NAME_FULL_RFP;
        } else if (YBBConstant.ODDS_TYPE_RFP_1ST.equalsIgnoreCase(oddsType)) {
            dishName = YBBConstant.CUSTOM_DISH_NAME_FIRST_HALF_RFP;
        }

        if (dishName != null) {
            String dishId = dishMapping.get(dishName);
            if (dishId != null) {
                // odds 以8个为一组
                int groupNum = odds.size() / 8;
                for (int i=0; i<groupNum; i++) {
                    int fromIndex = i * 8;
                    int toIndex = fromIndex + 8;
                    List<String> subOdds = odds.subList(fromIndex, toIndex);

                    // 主队让分
                    String homeTeamItem = subOdds.get(1);
                    homeTeamItem = getOddsItem(homeTeamItem);
                    // 客队让分
                    String guestTeamItem = subOdds.get(3);
                    guestTeamItem = getOddsItem(guestTeamItem);
                    // 主队让球赔率
                    String homeTeamOdds = subOdds.get(5);
                    homeTeamOdds = getOdds(homeTeamOdds);
                    // 客队让球赔率
                    String guestTeamOdds = subOdds.get(7);
                    guestTeamOdds = getOdds(guestTeamOdds);

                    YbbSports ybbSports = new YbbSports();
                    BeanUtils.copyProperties(initYbbSports, ybbSports);
                    ybbSports.setId(LangUtils.generateUuid());
                    ybbSports.setDishId(dishId);
                    ybbSports.setDishName(dishName);
                    ybbSports.setHomeTeamOdds(homeTeamOdds);
                    ybbSports.setGuestTeamOdds(guestTeamOdds);
                    ybbSports.setHomeTeamItem(homeTeamItem.toString());
                    ybbSports.setGuestTeamItem(guestTeamItem.toString());
                    ybbSportsList.add(ybbSports);
                }
            }
        }
        return ybbSportsList;
    }

    /**
     * 盘口处理方式 - 大小盘
     */
    private List<YbbSports> dishHandler4Dxp(YbbSports initYbbSports, String oddsType, List<String> odds, Map<String, String> dishMapping) {
        List<YbbSports> ybbSportsList = new ArrayList<>();

        String dishName = null;
        if (YBBConstant.ODDS_TYPE_DXP.equalsIgnoreCase(oddsType)) {
            dishName = YBBConstant.CUSTOM_DISH_NAME_FULL_DXP;
        } else if (YBBConstant.ODDS_TYPE_DXP_1ST.equalsIgnoreCase(oddsType)) {
            dishName = YBBConstant.CUSTOM_DISH_NAME_FIRST_HALF_DXP;
        }

        if (dishName != null) {
            String dishId = dishMapping.get(dishName);
            if (dishId != null) {
                // odds 以8个为一组
                int groupNum = odds.size() / 8;
                for (int i=0; i<groupNum; i++) {
                    int fromIndex = i * 8;
                    int toIndex = fromIndex + 8;
                    List<String> subOdds = odds.subList(fromIndex, toIndex);

                    // 大小数
                    String homeTeamItem = subOdds.get(1);
                    homeTeamItem = getOddsItem(homeTeamItem);
                    // 主队赔率
                    String homeTeamOdds = subOdds.get(5);
                    homeTeamOdds = getOdds(homeTeamOdds);
                    // 客队赔率
                    String guestTeamOdds = subOdds.get(7);
                    guestTeamOdds = getOdds(guestTeamOdds);

                    YbbSports ybbSports = new YbbSports();
                    BeanUtils.copyProperties(initYbbSports, ybbSports);
                    ybbSports.setId(LangUtils.generateUuid());
                    ybbSports.setDishId(dishId);
                    ybbSports.setDishName(dishName);
                    ybbSports.setHomeTeamOdds(homeTeamOdds);
                    ybbSports.setGuestTeamOdds(guestTeamOdds);
                    ybbSports.setHomeTeamItem(homeTeamItem.toString());
                    ybbSports.setHomeExtraDishName(YBBConstant.EXTRA_DISH_NAME_GREATER_THAN);
                    ybbSports.setGuestExtraDishName(YBBConstant.EXTRA_DISH_NAME_LESS_THAN);
                    ybbSportsList.add(ybbSports);
                }
            }
        }
        return ybbSportsList;
    }

    /**
     * 获取赔率 - 由于返回的香港盘的赔率，需要转换为欧洲盘赔率
     * @param odds
     * @return
     */
    private String getOdds(String odds) {
        Double d = CommonUtils.parseDouble(odds);
        BigDecimal bd = BigDecimal.valueOf(d);
        bd = bd.add(BigDecimal.valueOf(1));
        return bd.toString();
    }

    /**
     * 处理赔率Item
     * @param oddsItem
     * @return
     */
    private String getOddsItem(String oddsItem) {
        boolean negative = false;
        if (oddsItem.startsWith("-")) {
            negative = true;
        }

        oddsItem = oddsItem.replace("+", "");
        oddsItem = oddsItem.replace("-", "");

        // 需要特殊处理
        if (oddsItem.contains("/")) {
            List<String> temp = Arrays.asList(oddsItem.split("/"));
            Double d1 = CommonUtils.parseDouble(temp.get(0));
            Double d2 = CommonUtils.parseDouble(temp.get(1));
            Double d = (d1 + d2) / 2;

            oddsItem =  d.toString();
        }

        if (!oddsItem.contains(".")) {
            oddsItem += ".0";
        }

        return negative ? "-" + oddsItem : oddsItem;
    }

    /**
     * 处理开赛时间
     * @param startTime
     * @return
     */
    private String getStartTime(String startTime) {
        return startTime.replace("T", " ");
    }

    /**
     * 请求参数
     */
    private Map<String, Object> getFormData(boolean isToday) {
        Map<String, Object> params = new HashMap<>();
        params.put("IsFirstLoad", true);
        params.put("VersionL", -1);
        params.put("VersionU", 0);
        params.put("VersionS", -1);
        params.put("VersionF", -1);
        params.put("VersionH", 0);
        params.put("VersionT", -1);
        params.put("IsEventMenu", false);
        params.put("CompetitionID", -1);
        params.put("oIsInplayAll", false);
        params.put("oIsFirstLoad", true);
        params.put("oSortBy", 1);
        params.put("oOddsType", 0);
        params.put("oPageNo", 0);
        params.put("LiveCenterEventId", 0);
        params.put("LiveCenterSportId", 0);
        params.put("SportID", 1);
        if (isToday) {
            params.put("reqUrl", "/zh-cn/sports/football/matches-by-date/today/full-time-asian-handicap-and-over-under");
            params.put("hisUrl", "/zh-cn/sports/football/matches-by-date/today/full-time-asian-handicap-and-over-under?q=&country=CN&currency=RMB&tzoff=-240&reg=China&rc=CN&allowRacing=false");
        } else {
            params.put("reqUrl", "/zh-cn/sports/football/matches-by-date/tomorrow/full-time-asian-handicap-and-over-under");
            params.put("hisUrl", "/zh-cn/sports/football/matches-by-date/today/full-time-asian-handicap-and-over-under");
        }
        return params;
    }

    /**
     * 保存
     * @param ybbSports
     */
    private void saveYbbSports(List<YbbSports> ybbSports) {
        if (!CollectionUtils.isEmpty(ybbSports)) {
            ybbSportsRepository.saveAll(ybbSports);
            ybbSportsRepository.flush();
        }
    }
}
