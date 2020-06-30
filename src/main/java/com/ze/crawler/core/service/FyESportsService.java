package com.ze.crawler.core.service;

import com.alibaba.fastjson.JSON;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.FYConstant;
import com.ze.crawler.core.constants.ProxyConstant;
import com.ze.crawler.core.entity.FyEsports;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.FyEsportsRepository;
import com.ze.crawler.core.service.log.LogService;
import com.ze.crawler.core.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 泛亚电竞盘口
 */
@SuppressWarnings("all")
@Slf4j
@Service
public class FyESportsService implements BaseService {
    @Autowired
    private FyEsportsRepository fyEsportsRepository;
    @Autowired
    private LogService logService;

    @Override
    public void crawler(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        log.info("泛亚电竞_" + type + "_" + taskId);
        long startTime = System.currentTimeMillis();

        int retryCount = 0;
        while (true) {
            Map<String, String> headers = getRequestHeaders(FYConstant.PATH_MATCH_LIST);
            Map<String, Object> map = HttpClientUtils.postFrom(FYConstant.FY_BASE_URL, null, headers, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.ESPORTS_DISH_FY));
            if (!CollectionUtils.isEmpty(map)) {
                Map<String, Object> info = (Map<String, Object>) map.get("info");
                if (!CollectionUtils.isEmpty(info) && info.containsKey("Match")) {
                    List<Map<String, Object>> matchList = (List<Map<String, Object>>) info.get("Match");
                    try {
                        parseEsports(taskId, type, matchList, appointedLeagues, appointedTeams);
                    } catch (Exception e) {
                        Map<String, String> data = new HashMap<>();
                        data.put("url", FYConstant.FY_BASE_URL);
                        data.put("result", JSON.toJSONString(map));
                        data.put("retry_count", String.valueOf(retryCount));
                        logService.log(Constant.LOG_TYPE_PARSE_SPORTS_ERROR, Constant.SPORTS_DISH_YB.toString(), JSON.toJSONString(data), e);
                    }
                    break;
                }
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("泛亚电竞_" + type + "_" + taskId + "_[耗时（秒）: " + CommonUtils.getSeconds(endTime - startTime) + "]");
    }

    /**
     * 解析电竞
     */
    private void parseEsports(String taskId, String type, List<Map<String, Object>> matchList, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        // 第二天最后时刻
        String nextDayLastTime = TimeUtils.getNextDayLastTime();

        if (!CollectionUtils.isEmpty(matchList)) {
            // 遍历
            for (Map<String, Object> match : matchList) {
                // 比赛名
                String gameName = (String) match.get("GameName");

                // 根据type过滤想要的联赛
                if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_LOL)) {
                    if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_LOL)) {
                        continue;
                    }
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_DOTA2)) {
                    if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_DOTA2)) {
                        continue;
                    }
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_CSGO)) {
                    if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_CSGO)) {
                        continue;
                    }
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_KPL)) {
                    if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_KPL)) {
                        continue;
                    }
                } else {
                    // 其余赛事, 暂不需要
                    continue;
                }

                // 状态（只爬未开始的）
                String status = (String) match.get("Status");
                if (!FYConstant.MATCH_STATUS_BEGIN.equalsIgnoreCase(status)) {
                    continue;
                }

                // 比赛开始时间
                String startTime = (String) match.get("StartAt");
                if (nextDayLastTime.compareTo(startTime) < 0) {
                    continue;
                }

                // 联赛名
                String leagueName = (String) match.get("LeagueName");
                if (StringUtils.isEmpty(leagueName)) {
                    continue;
                }
                leagueName = leagueName.trim();
                String leagueId = Dictionary.ESPORT_FY_LEAGUE_MAPPING.get(leagueName);
                if (leagueId == null) {
                    continue;
                }

                // 如果存在指定联赛, 进行过滤判断
                if (!FilterUtils.filterLeague(appointedLeagues, leagueId)) {
                    continue;
                }

                // 主客队信息
                String homeTeamName = ((String) match.get("HomeName"));
                String guestTeamName = ((String) match.get("AwayName"));
                if (StringUtils.isEmpty(homeTeamName) || StringUtils.isEmpty(guestTeamName)) {
                    continue;
                }

                homeTeamName = homeTeamName.trim();
                guestTeamName = guestTeamName.trim();

                // 获取主客队信息
                String homeTeamId = Dictionary.ESPORT_FY_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                String guestTeamId = Dictionary.ESPORT_FY_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());
                if (homeTeamId == null || guestTeamId == null) {
                    continue;
                }

                // 如果存在指定队伍, 进行过滤判断
                if (!FilterUtils.filterTeam(appointedTeams, homeTeamId, guestTeamId)) {
                    continue;
                }

                // 比赛ID
                Integer matchId = (Integer) match.get("ID");
                // 轮数
                String round = (String) match.get("Type");

                // 初始化一个, 避免重复赋值
                FyEsports initFyEsports = new FyEsports();
                initFyEsports.setTaskId(taskId);
                initFyEsports.setType(type);
                initFyEsports.setLeagueId(leagueId);
                initFyEsports.setLeagueName(leagueName);
                initFyEsports.setHomeTeamId(homeTeamId);
                initFyEsports.setHomeTeamName(homeTeamName);
                initFyEsports.setGuestTeamId(guestTeamId);
                initFyEsports.setGuestTeamName(guestTeamName);
                initFyEsports.setStartTime(startTime);

                // 获取对应盘口字典表
                Map<String, String> dishMapping = Dictionary.getEsportDishMappingByTypeAndDishType(type, Constant.ESPORTS_DISH_FY);

                List<FyEsports> fyEsportsList = new ArrayList<>();
                // 获取赔率信息
                Map<String, String> headers = getRequestHeaders(FYConstant.PATH_MATCH_INFO);
                Map<String, Object> body = getRequestParams(matchId);
                Map<String, Object> dishMap = HttpClientUtils.postFrom(FYConstant.FY_BASE_URL, body, headers, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.ESPORTS_DISH_FY));
                if (!CollectionUtils.isEmpty(dishMap)) {
                    Map<String, Object> info = (Map<String, Object>) dishMap.get("info");
                    if (!CollectionUtils.isEmpty(info)) {
                        // 赔率数值
                        Map<String, Map<String, Object>> oddsItems = (Map<String, Map<String, Object>>) info.get("Items");
                        // 比赛信息
                        Map<String, Object> matchInfo = (Map<String, Object>) info.get("Match");
                        // 盘口信息
                        List<Map<String, Object>> bets = (List<Map<String, Object>>) matchInfo.get("Bets");
                        if (!CollectionUtils.isEmpty(bets)) {
                            for (Map<String, Object> bet : bets) {
                                // 地图名
                                String mapName = (String) bet.get("Round");
                                if (!doMap(mapName, round)) {
                                    continue;
                                }

                                /*
                                    对应的具体赔率
                                    1、输赢盘： 0是主队 1是客队
                                    2、大小盘： 0是大  2是小
                                    3、让分盘： 0是主队 1是客队
                                    4、单双盘： 0是单 1是双
                                    5、是否盘： 0是是 1是否
                                 */
                                List<Map<String, Object>> items = (List<Map<String, Object>>) bet.get("Items");
                                if (CollectionUtils.isEmpty(items) || items.size() != 2) {
                                    continue;
                                }

                                String handicap = (String) bet.get("Handicap");
                                String name = (String) bet.get("Name");
                                // 盘口名
                                String dishName = getDishName(handicap, name);
                                String dishId = dishMapping.get(dishName);
                                if (dishId != null) {
                                    String dishType = Dictionary.ESPORT_DISH_TYPE_MAPPING.get(dishId);
                                    if (dishType == null) {
                                        continue;
                                    }

                                    FyEsports fyEsports = new FyEsports();
                                    BeanUtils.copyProperties(initFyEsports, fyEsports);
                                    fyEsports.setId(LangUtils.generateUuid());
                                    fyEsports.setDishId(dishId);
                                    fyEsports.setDishName(dishName);

                                    String odds1 = getOdds(items, oddsItems, FYConstant.INDEX_FIRST);
                                    String odds2 = getOdds(items, oddsItems, FYConstant.INDEX_SECOND);
                                    if (StringUtils.isEmpty(odds1) || StringUtils.isEmpty(odds1)) {
                                        continue;
                                    }

                                    if (Constant.DISH_TYPE_SYP.equals(dishType)) {
                                        // 输赢盘
                                        fyEsports.setHomeTeamOdds(odds1);
                                        fyEsports.setGuestTeamOdds(odds2);
                                    } else if (Constant.DISH_TYPE_RFP.equals(dishType)) {
                                        // 让分盘
                                        String homeTeamItem = handicap;
                                        if (handicap.startsWith("+")) {
                                            homeTeamItem = homeTeamItem.replace("+", "");
                                        }
                                        String guestTeamItem = handicap;
                                        if (handicap.startsWith("+")) {
                                            guestTeamItem = guestTeamItem.replace("+", "-");
                                        } else {
                                            guestTeamItem = guestTeamItem.replace("-", "");
                                        }

                                        fyEsports.setHomeTeamOdds(odds1);
                                        fyEsports.setGuestTeamOdds(odds2);
                                        fyEsports.setHomeTeamItem(homeTeamItem);
                                        fyEsports.setGuestTeamItem(guestTeamItem);
                                    } else if (Constant.DISH_TYPE_DXP.equals(dishType) || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                                        // 大小盘
                                        String homeTeamItem = handicap;
                                        if (handicap.contains(":")) {
                                            // 比赛时长大小特殊处理
                                            homeTeamItem = homeTeamItem.replace(":00", ".0");
                                        }

                                        fyEsports.setHomeTeamOdds(odds1);
                                        fyEsports.setGuestTeamOdds(odds2);
                                        fyEsports.setHomeTeamItem(homeTeamItem);
                                        fyEsports.setHomeExtraDishName(FYConstant.EXTRA_DISH_NAME_GREATER_THAN);
                                        fyEsports.setGuestExtraDishName(FYConstant.EXTRA_DISH_NAME_LESS_THAN);
                                    } else if (Constant.DISH_TYPE_DSP.equals(dishType)) {
                                        // 单双盘
                                        fyEsports.setHomeTeamOdds(odds1);
                                        fyEsports.setGuestTeamOdds(odds2);
                                        fyEsports.setHomeExtraDishName(FYConstant.EXTRA_DISH_NAME_ODD);
                                        fyEsports.setGuestExtraDishName(FYConstant.EXTRA_DISH_NAME_EVEN);
                                    } else if (Constant.DISH_TYPE_SFP.equals(dishType)) {
                                        // 是否盘
                                        fyEsports.setHomeTeamOdds(odds2);
                                        fyEsports.setGuestTeamOdds(odds1);
                                        fyEsports.setHomeExtraDishName(FYConstant.EXTRA_DISH_NAME_NO);
                                        fyEsports.setGuestExtraDishName(FYConstant.EXTRA_DISH_NAME_YES);
                                    }

                                    fyEsportsList.add(fyEsports);
                                }
                            }
                        }
                    }
                }
                // 保存
                saveFyEsports(fyEsportsList);
            }
        }
    }

    /**
     * 获取对应赔率
     */
    private String getOdds(List<Map<String, Object>> items, Map<String, Map<String, Object>> oddsItems, int index) {
        Map<String, Object> item = items.get(index);
        Integer oddsId = (Integer) item.get("ID");

        if (oddsItems.containsKey(oddsId.toString())) {
            Map<String, Object> oddsItem = oddsItems.get(oddsId.toString());
            Integer isLock = (Integer) oddsItem.get("IsLock");
            if (isLock == FYConstant.IS_LOCK) {
                return null;
            }

            BigDecimal odds = (BigDecimal) oddsItem.get("Odds");
            double o = CommonUtils.setScale(odds.doubleValue(), 3);
            return BigDecimal.valueOf(o).toString();
        }
        return null;
    }

    /**
     * 构造盘口名
     */
    private String getDishName(String handicap, String name) {
        String dishName = name;
        if (!StringUtils.isEmpty(handicap)) {
            dishName = dishName.replace(handicap, "");
            dishName = dishName.trim();
        }
        return dishName;
    }

    /**
     * 过滤场次
     */
    private boolean doMap(String mapName, String round) {
        if (FYConstant.ROUND_BO5.equals(round) || FYConstant.ROUND_BO7.equals(round)) {
            // bo5 or bo7
            if (mapName.equals(FYConstant.ROUND_MAP4) || mapName.equals(FYConstant.ROUND_MAP5)) {
                return false;
            }
        } else if (FYConstant.ROUND_BO3.equals(round)) {
            // bo3
            if (mapName.equals(FYConstant.ROUND_MAP3)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 请求Body
     */
    private Map<String, Object> getRequestParams(Integer matchId) {
        Map<String, Object> params = new HashMap<>();
        params.put("matchId", matchId);
        return params;
    }

    /**
     * 获取请求头
     */
    private Map<String, String> getRequestHeaders(String path) {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("accept-encoding", "gzip, deflate, br");
        headers.put("accept-language", "zh-CN,zh;q=0.9");
        headers.put("ghost", "60e1601dc3964090ac33e4d55ffe0bbe");
        headers.put("origin", "https://jingjib.aabv.top");
        headers.put("path", path);
        headers.put("referer", "https://jingjib.yqb-sc.top/index.html?v=1.2.51");
        headers.put("x-forwarded-host", "jingjib.yqb-sc.top");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "cross-site");

        return headers;
    }

    /**
     * 保存
     * @param fyEsports
     */
    private void saveFyEsports(List<FyEsports> fyEsports) {
        if (!CollectionUtils.isEmpty(fyEsports)) {
            fyEsportsRepository.saveAll(fyEsports);
            fyEsportsRepository.flush();
        }
    }
}
