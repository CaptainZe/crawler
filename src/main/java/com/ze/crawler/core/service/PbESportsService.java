package com.ze.crawler.core.service;

import com.alibaba.fastjson.JSON;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.PBConstant;
import com.ze.crawler.core.entity.PbEsports;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.PbEsportsRepository;
import com.ze.crawler.core.service.log.LogService;
import com.ze.crawler.core.utils.FilterUtils;
import com.ze.crawler.core.utils.HttpClientUtils;
import com.ze.crawler.core.utils.LangUtils;
import com.ze.crawler.core.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 平博盘口 - 电竞
 */
@SuppressWarnings("all")
@Slf4j
@Service
public class PbESportsService implements BaseService {
    @Autowired
    private PbEsportsRepository pbEsportsRepository;
    @Autowired
    private LogService logService;

    /**
     * 平博电竞爬虫
     * @param taskId
     * @param type  赛事类型，LOL、DOTA2、CSGO
     * @param appointedLeagues  指定联赛
     * @param appointedTeams    指定队伍
     */
    @Override
    public void crawler(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        log.info("平博电竞_" + type + "_" + taskId);

        int retryCount = 0;
        while (true) {
            // 今天
            String url = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_TODAY, PBConstant.SP_ESPORTS, TimeUtils.getDate(), System.currentTimeMillis());
            Map<String, Object> map = HttpClientUtils.get(url, Map.class);
            if (map != null && map.get("n") != null && !CollectionUtils.isEmpty((List<Object>) map.get("n"))) {
                try {
                    parseEsports(taskId, type, map, appointedLeagues, appointedTeams);
                } catch (Exception e) {
                    Map<String, String> data = new HashMap<>();
                    data.put("url", url);
                    data.put("result", JSON.toJSONString(map));
                    data.put("retry_count", String.valueOf(retryCount));
                    logService.log(Constant.LOG_TYPE_PARSE_ESPORTS_ERROR, Constant.ESPORTS_DISH_PB.toString(), JSON.toJSONString(data), e);
                }
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                break;
            }
        }

        retryCount = 0;
        while (true) {
            // 早盘
            String url = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_ZP, PBConstant.SP_ESPORTS, TimeUtils.getDate(), System.currentTimeMillis());
            Map<String, Object> map = HttpClientUtils.get(url, Map.class);
            if (map != null && map.get("n") != null && !CollectionUtils.isEmpty((List<Object>) map.get("n"))) {
                try {
                    parseEsports(taskId, type, map, appointedLeagues, appointedTeams);
                } catch (Exception e) {
                    Map<String, String> data = new HashMap<>();
                    data.put("url", url);
                    data.put("result", JSON.toJSONString(map));
                    data.put("retry_count", String.valueOf(retryCount));
                    logService.log(Constant.LOG_TYPE_PARSE_ESPORTS_ERROR, Constant.ESPORTS_DISH_PB.toString(), JSON.toJSONString(data), e);
                }
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                break;
            }
        }
    }

    /**
     * 电竞解析
     * @param taskId
     * @param type
     * @param map
     */
    private void parseEsports(String taskId, String type, Map<String, Object> map, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        if (map != null) {
            List<Object> n = (List<Object>) map.get("n");
            if (!CollectionUtils.isEmpty(n)) {
                // eSports 电竞盘列表 (1是E Sports 2是联赛列表)
                List<Object> eSports = (List<Object>) n.get(0);
                if (!CollectionUtils.isEmpty(eSports)) {
                    // 联赛列表 (这个是全部电竞的列表, 包含LOL,DOTA2,CSGO等, 需要根据type过滤想要的)
                    List<List<Object>> leagues = (List<List<Object>>) eSports.get(2);
                    if (!CollectionUtils.isEmpty(leagues)) {
                        // 遍历联赛列表
                        for (List<Object> league : leagues) {
                            // 联赛名, 比如: 英雄联盟 - 中国LPL
                            String leagueName = ((String) league.get(1)).trim();

                            // 根据type过滤想要的联赛
                            if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_LOL)) {
                                if (!leagueName.startsWith(PBConstant.LEAGUE_PREFIX_LOL)
                                        && !leagueName.startsWith(PBConstant.LEAGUE_PREFIX_LOL_EN)) {
                                    continue;
                                }
                            } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_DOTA2)) {
                                if (!leagueName.startsWith(PBConstant.LEAGUE_PREFIX_DOTA2)) {
                                    continue;
                                }
                            } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_CSGO)) {
                                if (!leagueName.startsWith(PBConstant.LEAGUE_PREFIX_CSGO)) {
                                    continue;
                                }
                            } else {
                                // 其余赛事, 比如王者荣耀, 暂不需要
                                continue;
                            }

                            // 赛事信息获取
                            if (!Dictionary.ESPORT_PB_LEAGUE_MAPPING.containsKey(leagueName)) {
                                logService.log(Constant.LOG_TYPE_LEAGUE_NOT_FOUND, Constant.ESPORTS_DISH_PB.toString(), type + ":" + leagueName);
                                continue;
                            }
                            String leagueId = Dictionary.ESPORT_PB_LEAGUE_MAPPING.get(leagueName);
                            if (leagueId == null) {
                                continue;
                            }

                            // 如果存在指定联赛, 进行过滤判断
                            if (!FilterUtils.filterLeague(appointedLeagues, leagueId)) {
                                continue;
                            }

                            // 具体比赛列表
                            List<List<Object>> games = (List<List<Object>>) league.get(2);
                            if (!CollectionUtils.isEmpty(games)) {
                                for (List<Object> game : games) {
                                    // me参数
                                    Integer meParam = (Integer) game.get(0);
                                    // 开赛时间
                                    Long startTimestamp = (Long) game.get(4);
                                    String startTime = TimeUtils.format(startTimestamp);
                                    // home team name
                                    String homeTeamName = (String) game.get(1);
                                    // guest team name
                                    String guestTeamName = (String) game.get(2);

                                    if (StringUtils.isEmpty(homeTeamName) || StringUtils.isEmpty(guestTeamName)) {
                                        continue;
                                    }
                                    String matchHomeTeamName = null;
                                    String matchGuestTeamName = null;
                                    if (isKill(homeTeamName)) {
                                        matchHomeTeamName = homeTeamName.replace(PBConstant.TEAM_NAME_KILL_SUFFIX, "").trim().toUpperCase();
                                        matchGuestTeamName = guestTeamName.replace(PBConstant.TEAM_NAME_KILL_SUFFIX, "").trim().toUpperCase();
                                    } else {
                                        matchHomeTeamName = homeTeamName.toUpperCase();
                                        matchGuestTeamName = guestTeamName.toUpperCase();
                                    }

                                    if (StringUtils.isEmpty(matchHomeTeamName) || StringUtils.isEmpty(matchGuestTeamName)) {
                                        continue;
                                    }

                                    // 获取主客队信息
                                    String homeTeamId = Dictionary.ESPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).get(matchHomeTeamName);
                                    String guestTeamId = Dictionary.ESPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).get(matchGuestTeamName);
                                    if (homeTeamId == null || guestTeamId == null) {
                                        if (homeTeamId == null) {
                                            logService.log(Constant.LOG_TYPE_TEAM_NOT_FOUND, Constant.ESPORTS_DISH_PB.toString(), type + ":" + leagueId + "#" + homeTeamName);
                                        }
                                        if (guestTeamId == null) {
                                            logService.log(Constant.LOG_TYPE_TEAM_NOT_FOUND, Constant.ESPORTS_DISH_PB.toString(), type + ":" + leagueId + "#" + guestTeamName);
                                        }
                                        continue;
                                    }

                                    // 如果存在指定队伍, 进行过滤判断
                                    if (!FilterUtils.filterTeam(appointedTeams, homeTeamId, guestTeamId)) {
                                        continue;
                                    }

                                    PbEsports initPbEsports = new PbEsports();
                                    initPbEsports.setTaskId(taskId);
                                    initPbEsports.setType(type);
                                    initPbEsports.setLeagueId(leagueId);
                                    initPbEsports.setLeagueName(leagueName);
                                    initPbEsports.setHomeTeamId(homeTeamId);
                                    initPbEsports.setHomeTeamName(homeTeamName);
                                    initPbEsports.setGuestTeamId(guestTeamId);
                                    initPbEsports.setGuestTeamName(guestTeamName);
                                    initPbEsports.setStartTime(startTime);

                                    if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_LOL) || type.equalsIgnoreCase(Constant.ESPORTS_TYPE_DOTA2)) {
                                        // second team name (用于判断是不是（击杀数）)
                                        boolean isKill = isKill(homeTeamName);

                                        // 具体赔率等信息. (KEY: 0表示全场 1表示地图1 2表示地图2 3表示地图3 4表示地图4 5表示地图5)
                                        Map<String, List<Object>> dishMap = (Map<String, List<Object>>) game.get(8);
                                        if (!isKill) {
                                            dealLoLOrDotaOrCsGoNormal(meParam, initPbEsports, dishMap);
                                        } else {
                                            dealLoLOrDotaKill(meParam, initPbEsports);
                                        }
                                    } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_CSGO)) {
                                        // 具体赔率等信息. (KEY: 0表示全场 1表示地图1 2表示地图2 3表示地图3 4表示地图4 5表示地图5)
                                        Map<String, List<Object>> dishMap = (Map<String, List<Object>>) game.get(8);
                                        dealLoLOrDotaOrCsGoNormal(meParam, initPbEsports, dishMap);
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
     * LOL & Dota 2 & CSGO 使用
     * 处理非（击杀数）盘的数据
     * @param dishMap   KEY: 0表示全场 1表示地图1 2表示地图2 3表示地图3 4表示地图4 5表示地图5
     */
    private void dealLoLOrDotaOrCsGoNormal(Integer meParam, PbEsports initPbEsports, Map<String, List<Object>> dishMap) {
        // 获取对应盘口字典表
        Map<String, String> dishMapping = Dictionary.getEsportDishMappingByTypeAndDishType(initPbEsports.getType(), Constant.ESPORTS_DISH_PB);
        // 爬取的最大地图数
        int maxMap = 1;

        List<PbEsports> pbEsportsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dishMap)) {
            maxMap = getMaxMap(dishMap);

            for (String key : dishMap.keySet()) {
                if (!doMap(key, dishMap)) {
                    continue;
                }

                // 0表示让分盘 1表示大小盘 2表示输赢盘
                List<Object> dishDetailInfos = dishMap.get(key);
                if (key.equals("0")) {
                    // 全场才有让分盘和大小盘
                    // 1.让分盘    可能有多个数据
                    List<List<Object>> oddsInfos = (List<List<Object>>) dishDetailInfos.get(0);
                    if (!CollectionUtils.isEmpty(oddsInfos)) {
                        // 自定义盘口名
                        String dishName = PBConstant.CUSTOM_DISH_NAME_WHOLE_RFP;
                        String dishId = dishMapping.get(dishName);
                        if (dishId != null) {
                            for (List<Object> oddsInfo : oddsInfos) {
                                if (!CollectionUtils.isEmpty(oddsInfo)) {
                                    // 主队让分数
                                    BigDecimal homeTeamItem = (BigDecimal) oddsInfo.get(1);
                                    // 客队让分数
                                    BigDecimal guestTeamItem = (BigDecimal) oddsInfo.get(0);
                                    // 主队赔率
                                    String homeTeamOdds = (String) oddsInfo.get(3);
                                    // 客队赔率
                                    String guestTeamOdds = (String) oddsInfo.get(4);

                                    PbEsports pbEsports = new PbEsports();
                                    BeanUtils.copyProperties(initPbEsports, pbEsports);
                                    pbEsports.setId(LangUtils.generateUuid());
                                    pbEsports.setDishId(dishId);
                                    pbEsports.setDishName(dishName);
                                    pbEsports.setHomeTeamOdds(homeTeamOdds);
                                    pbEsports.setGuestTeamOdds(guestTeamOdds);
                                    pbEsports.setHomeTeamItem(homeTeamItem.toString());
                                    pbEsports.setGuestTeamItem(guestTeamItem.toString());
                                    pbEsportsList.add(pbEsports);
                                }
                            }
                        }
                    }

                    // 2.大小盘
                    List<List<Object>> oddsInfos4DX = (List<List<Object>>) dishDetailInfos.get(1);
                    if (!CollectionUtils.isEmpty(oddsInfos4DX)) {
                        // 自定义盘口名
                        String dishName = PBConstant.CUSTOM_DISH_NAME_WHOLE_DXP;
                        String dishId = dishMapping.get(dishName);
                        if (dishId != null) {
                            for (List<Object> oddsInfo : oddsInfos4DX) {
                                if (!CollectionUtils.isEmpty(oddsInfo)) {
                                    // 大小数
                                    String dxItem = (String) oddsInfo.get(0);
                                    // 主队赔率
                                    String homeTeamOdds = (String) oddsInfo.get(2);
                                    // 客队赔率
                                    String guestTeamOdds = (String) oddsInfo.get(3);

                                    PbEsports pbEsports = new PbEsports();
                                    BeanUtils.copyProperties(initPbEsports, pbEsports);
                                    pbEsports.setId(LangUtils.generateUuid());
                                    pbEsports.setDishId(dishId);
                                    pbEsports.setDishName(dishName);
                                    pbEsports.setHomeTeamOdds(homeTeamOdds);
                                    pbEsports.setGuestTeamOdds(guestTeamOdds);
                                    pbEsports.setHomeTeamItem(dxItem);
                                    pbEsports.setHomeExtraDishName(PBConstant.EXTRA_DISH_NAME_DP);
                                    pbEsports.setGuestExtraDishName(PBConstant.EXTRA_DISH_NAME_XP);
                                    pbEsportsList.add(pbEsports);
                                }
                            }
                        }
                    }
                }
                // 3.输赢盘
                String sypDishName = getDishNameByKey(key);
                if (sypDishName != null) {
                    String sypDishId = dishMapping.get(sypDishName);
                    if (sypDishId != null) {
                        List<Object> oddsInfos4SY = (List<Object>) dishDetailInfos.get(2);
                        if (!CollectionUtils.isEmpty(oddsInfos4SY)) {
                            // 主队赔率
                            String homeTeamOdds = (String) oddsInfos4SY.get(1);
                            // 客队赔率
                            String guestTeamOdds = (String) oddsInfos4SY.get(0);

                            PbEsports pbEsports = new PbEsports();
                            BeanUtils.copyProperties(initPbEsports, pbEsports);
                            pbEsports.setId(LangUtils.generateUuid());
                            pbEsports.setDishId(sypDishId);
                            pbEsports.setDishName(sypDishName);
                            pbEsports.setHomeTeamOdds(homeTeamOdds);
                            pbEsports.setGuestTeamOdds(guestTeamOdds);
                            pbEsportsList.add(pbEsports);
                        }
                    }
                }
            }
        }
        // 保存爬取的数据
        savePbEsports(pbEsportsList);

        // 获取更多盘
        int retryCount = 0;
        while (true) {
            String moreUrl = String.format(PBConstant.PB_MORE_URL, PBConstant.MK_MORE, meParam, System.currentTimeMillis());
            Map<String, List<Object>> moreMap = HttpClientUtils.get(moreUrl, Map.class);
            if (moreMap != null && moreMap.get("e") != null && !CollectionUtils.isEmpty((List<Object>) moreMap.get("e"))) {
                try {
                    if (initPbEsports.getType().equalsIgnoreCase(Constant.ESPORTS_TYPE_LOL) || initPbEsports.getType().equalsIgnoreCase(Constant.ESPORTS_TYPE_DOTA2)) {
                        dealLoLOrDotaNormalMore(initPbEsports, moreMap, maxMap);
                        break;
                    } else if (initPbEsports.getType().equalsIgnoreCase(Constant.ESPORTS_TYPE_CSGO)) {
                        dealCsGoMore(initPbEsports, moreMap, maxMap);
                        break;
                    }
                } catch (Exception e) {
                    Map<String, String> data = new HashMap<>();
                    data.put("url", moreUrl);
                    data.put("result", JSON.toJSONString(moreMap));
                    data.put("retry_count", String.valueOf(retryCount));
                    logService.log(Constant.LOG_TYPE_PARSE_ESPORTS_ERROR, Constant.ESPORTS_DISH_PB.toString(), JSON.toJSONString(data), e);
                    break;
                }
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                break;
            }
        }
    }

    /**
     * 处理非（击杀数）盘的数据 - 更多
     * @param moreMap
     */
    private void dealLoLOrDotaNormalMore(PbEsports initPbEsports, Map<String, List<Object>> moreMap, int maxMap) {
        // 获取对应盘口字典表
        Map<String, String> dishMapping = Dictionary.getEsportDishMappingByTypeAndDishType(initPbEsports.getType(), Constant.ESPORTS_DISH_PB);

        List<PbEsports> pbEsportsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(moreMap)) {
            List<Object> e = moreMap.get("e");
            if (!CollectionUtils.isEmpty(e)) {
                List<Object> list = (List<Object>) e.get(3);
                if (!CollectionUtils.isEmpty(list)) {
                    Map<String, Object> moreDishMap = (Map<String, Object>) list.get(8);
                    if (!CollectionUtils.isEmpty(moreDishMap)) {
                        // 取其中一个key下的数据即可
                        List<Object> moreDishList = (List<Object>) moreDishMap.get(moreDishMap.keySet().iterator().next());
                        if (!CollectionUtils.isEmpty(moreDishList)) {
                            // 可能有更多让分盘
                            List<List<Object>> moreRfpDishList = (List<List<Object>>) moreDishList.get(2);
                            if (!CollectionUtils.isEmpty(moreRfpDishList)) {
                                // 自定义盘口名
                                String dishName = PBConstant.CUSTOM_DISH_NAME_WHOLE_RFP;
                                String dishId = dishMapping.get(dishName);
                                if (dishId != null) {
                                    for (List<Object> oddsInfo : moreRfpDishList) {
                                        if (!CollectionUtils.isEmpty(oddsInfo)) {
                                            // 主队让分数
                                            BigDecimal homeTeamItem = (BigDecimal) oddsInfo.get(1);
                                            // 客队让分数
                                            BigDecimal guestTeamItem = (BigDecimal) oddsInfo.get(0);
                                            // 主队赔率
                                            String homeTeamOdds = (String) oddsInfo.get(3);
                                            // 客队赔率
                                            String guestTeamOdds = (String) oddsInfo.get(4);

                                            PbEsports pbEsports = new PbEsports();
                                            BeanUtils.copyProperties(initPbEsports, pbEsports);
                                            pbEsports.setId(LangUtils.generateUuid());
                                            pbEsports.setDishId(dishId);
                                            pbEsports.setDishName(dishName);
                                            pbEsports.setHomeTeamOdds(homeTeamOdds);
                                            pbEsports.setGuestTeamOdds(guestTeamOdds);
                                            pbEsports.setHomeTeamItem(homeTeamItem.toString());
                                            pbEsports.setGuestTeamItem(guestTeamItem.toString());
                                            pbEsportsList.add(pbEsports);
                                        }
                                    }
                                }
                            }

                            // 特殊盘
                            List<Object> dishDetailList = (List<Object>) moreDishList.get(1);
                            if (!CollectionUtils.isEmpty(dishDetailList)) {
                                Map<String, Object> dishDetailMap = (Map<String, Object>) dishDetailList.get(0);
                                if (!CollectionUtils.isEmpty(dishDetailMap)) {
                                    List<Map<String, Object>> seList = (List<Map<String, Object>>) dishDetailMap.get("se");
                                    if (!CollectionUtils.isEmpty(seList)) {
                                        for (Map<String, Object> se : seList) {
                                            // 盘口名
                                            String dishName = (String) se.get("n");
                                            String dishId = dishMapping.get(dishName);
                                            if (dishId == null) {
                                                continue;
                                            }
                                            if (!doMap4More(maxMap, dishName)) {
                                                continue;
                                            }
                                            // 具体赔率
                                            List<Map<String, Object>> oddsList = (List<Map<String, Object>>) se.get("l");
                                            if (CollectionUtils.isEmpty(oddsList)) {
                                                continue;
                                            }

                                            PbEsports pbEsports = new PbEsports();
                                            BeanUtils.copyProperties(initPbEsports, pbEsports);
                                            pbEsports.setId(LangUtils.generateUuid());
                                            pbEsports.setDishId(dishId);
                                            pbEsports.setDishName(dishName);

                                            // 各个盘具体处理方式不太一样,只能一个个单独处理
                                            String dishType = Dictionary.ESPORT_DISH_TYPE_MAPPING.get(dishId);
                                            if (dishType == null) {
                                                continue;
                                            }

                                            if (Constant.DISH_TYPE_SYP.equals(dishType)) {
                                                // 队名判断
                                                // 比如： (地图 1) 第一条纳什男爵
                                                moreDishHandler1(pbEsports, pbEsports.getHomeTeamName(), oddsList);
                                                pbEsportsList.add(pbEsports);
                                            } else if (Constant.DISH_TYPE_SFP.equals(dishType)) {
                                                // 不是/是
                                                // 比如：(地图 1) 双方分别击杀一条纳什男爵
                                                moreDishHandler2(pbEsports, oddsList);
                                                pbEsportsList.add(pbEsports);
                                            } else if (Constant.DISH_TYPE_DXP.equals(dishType) || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                                                // 大小盘
                                                // 比如：(地图 1) 游戏持续时间
                                                // 小盘后缀
                                                String un = (String) se.get("un");
                                                moreDishHandler3(pbEsports, oddsList, un);
                                                pbEsportsList.add(pbEsports);
                                            } else if (Constant.DISH_TYPE_DSP.equals(dishType)) {
                                                // 单双盘
                                                // 比如：(地图 1) 总杀人数为单数/双数
                                                moreDishHandler4(pbEsports, oddsList);
                                                pbEsportsList.add(pbEsports);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // 保存爬取的数据
        savePbEsports(pbEsportsList);
    }

    /**
     * 更多盘口的处理方式之1 - 根据队伍名存储
     * @param pbEsports
     * @param homeTeamName
     * @param oddsList
     */
    private void moreDishHandler1(PbEsports pbEsports, String homeTeamName, List<Map<String, Object>> oddsList) {
        for (Map<String, Object> oddsInfo : oddsList) {
            String teamName = (String) oddsInfo.get("n");
            BigDecimal odds = (BigDecimal) oddsInfo.get("p");
            if (teamName.equalsIgnoreCase(homeTeamName)) {
                pbEsports.setHomeTeamOdds(odds.toString());
            } else if (teamName.startsWith(homeTeamName)) {
                // fixme 需要数据验证
                // 有的队伍显示的名称和战队名不太一样, 比如有的后面有中文名等
                pbEsports.setHomeTeamOdds(odds.toString());
            } else {
                pbEsports.setGuestTeamOdds(odds.toString());
            }
        }
    }

    /**
     * 更多盘口的处理方式之2 - 不是/是
     * @param pbEsports
     * @param oddsList
     */
    private void moreDishHandler2(PbEsports pbEsports, List<Map<String, Object>> oddsList) {
        for (Map<String, Object> oddsInfo : oddsList) {
            String n = (String) oddsInfo.get("n");
            BigDecimal odds = (BigDecimal) oddsInfo.get("p");
            if ("不是".equals(n)) {
                pbEsports.setHomeTeamOdds(odds.toString());
                pbEsports.setHomeExtraDishName(n);
            } else {
                pbEsports.setGuestTeamOdds(odds.toString());
                pbEsports.setGuestExtraDishName(n);
            }
        }
    }

    /**
     * 更多盘口的处理方式之3 - 大小盘
     * @param pbEsports
     * @param oddsList
     * @param un
     */
    private void moreDishHandler3(PbEsports pbEsports, List<Map<String, Object>> oddsList, String un) {
        for (Map<String, Object> oddsInfo : oddsList) {
            String n = (String) oddsInfo.get("n");
            BigDecimal h = (BigDecimal) oddsInfo.get("h");
            BigDecimal odds = (BigDecimal) oddsInfo.get("p");
            if ("大盘".equals(n)) {
                pbEsports.setHomeTeamOdds(odds.toString());
                pbEsports.setHomeTeamItem(h.toString());
//                pbEsports.setHomeExtraDishName(n + h + un);
                pbEsports.setHomeExtraDishName(n);
            } else {
                pbEsports.setGuestTeamOdds(odds.toString());
//                pbEsports.setGuestExtraDishName(n + h + un);
                pbEsports.setGuestExtraDishName(n);
            }
        }
    }

    /**
     * 更多盘口的处理方式之4 - 单双盘
     * @param pbEsports
     * @param oddsList
     */
    private void moreDishHandler4(PbEsports pbEsports, List<Map<String, Object>> oddsList) {
        for (Map<String, Object> oddsInfo : oddsList) {
            String n = (String) oddsInfo.get("n");
            BigDecimal odds = (BigDecimal) oddsInfo.get("p");
            if ("单".equals(n)) {
                pbEsports.setHomeTeamOdds(odds.toString());
                pbEsports.setHomeExtraDishName(n);
            } else {
                pbEsports.setGuestTeamOdds(odds.toString());
                pbEsports.setGuestExtraDishName(n);
            }
        }
    }

    /**
     * LOL&Dota 2使用
     * 处理（击杀数）盘的数据
     * @param dishMap   KEY: 0表示全场 1表示地图1 2表示地图2 3表示地图3 4表示地图4 5表示地图5
     */
    private void dealLoLOrDotaKill(Integer meParam, PbEsports initPbEsports) {
        // 获取更多盘
        int retryCount = 0;
        while (true) {
            String moreUrl = String.format(PBConstant.PB_MORE_URL, PBConstant.MK_MORE, meParam, System.currentTimeMillis());
            Map<String, List<Object>> moreMap = HttpClientUtils.get(moreUrl, Map.class);
            if (moreMap != null && moreMap.get("e") != null && !CollectionUtils.isEmpty((List<Object>) moreMap.get("e"))) {
                try {
                    dealLoLOrDotaKillMore(initPbEsports, moreMap);
                } catch (Exception e) {
                    Map<String, String> data = new HashMap<>();
                    data.put("url", moreUrl);
                    data.put("result", JSON.toJSONString(moreMap));
                    data.put("retry_count", String.valueOf(retryCount));
                    logService.log(Constant.LOG_TYPE_PARSE_ESPORTS_ERROR, Constant.ESPORTS_DISH_PB.toString(), JSON.toJSONString(data), e);
                }
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                break;
            }
        }
    }

    /**
     * 处理（击杀数）盘的数据 - 更多
     */
    private void dealLoLOrDotaKillMore(PbEsports initPbEsports, Map<String, List<Object>> moreMap) {
        // 获取对应盘口字典表
        Map<String, String> dishMapping = Dictionary.getEsportDishMappingByTypeAndDishType(initPbEsports.getType(), Constant.ESPORTS_DISH_PB);

        List<PbEsports> pbEsportsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(moreMap)) {
            List<Object> e = moreMap.get("e");
            if (!CollectionUtils.isEmpty(e)) {
                List<Object> list = (List<Object>) e.get(3);
                if (!CollectionUtils.isEmpty(list)) {
                    Map<String, Object> moreDishMap = (Map<String, Object>) list.get(8);
                    if (!CollectionUtils.isEmpty(moreDishMap)) {
                        // 初始化一个, 后续不需要重复赋值
                        PbEsports pbEsports = new PbEsports();
                        BeanUtils.copyProperties(initPbEsports, pbEsports);

                        for (String key : moreDishMap.keySet()) {
                            if (!doMap4Kill(key, moreDishMap)) {
                                continue;
                            }

                            // 0球队总得分 1se(细盘)  2让分盘 3大小盘
                            List<Object> moreDishList = (List<Object>) moreDishMap.get(key);
                            if (!CollectionUtils.isEmpty(moreDishList)) {
                                // 1. 球队总得分     0主队 1客队
                                List<Object> totalKill = (List<Object>) moreDishList.get(0);
                                List<PbEsports> totalKillPbEsports = moreKillDishHandler1(pbEsports, key, totalKill, dishMapping);
                                if (!CollectionUtils.isEmpty(totalKillPbEsports)) {
                                    pbEsportsList.addAll(totalKillPbEsports);
                                }

                                // 2. 让分盘
                                List<List<Object>> rfpList = (List<List<Object>>) moreDishList.get(2);
                                List<PbEsports> rfpPbEsports = moreKillDishHandler2(pbEsports, key, rfpList, dishMapping);
                                if (!CollectionUtils.isEmpty(rfpPbEsports)) {
                                    pbEsportsList.addAll(rfpPbEsports);
                                }

                                // 3. 大小盘
                                List<List<Object>> dxpList = (List<List<Object>>) moreDishList.get(3);
                                List<PbEsports> dxpPbEsports = moreKillDishHandler3(pbEsports, key, dxpList, dishMapping);
                                if (!CollectionUtils.isEmpty(dxpPbEsports)) {
                                    pbEsportsList.addAll(dxpPbEsports);
                                }
                            }
                        }
                    }
                }
            }
        }
        // 保存
        savePbEsports(pbEsportsList);
    }

    /**
     * （击杀数）更多盘口的处理方式之1 - 球队总得分
     * @param totalKill     0主队 1客队
     * @return
     */
    private List<PbEsports> moreKillDishHandler1(PbEsports pbEsports, String key, List<Object> totalKill, Map<String, String> dishMapping) {
        List<PbEsports> pbEsportsList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(totalKill)) {
            // 1. 主队进球
            String homeDishName = null;
            if ("1".equals(key)) {
                homeDishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP1_HOME_TEAM_TOTAL;
            } else if ("2".equals(key)) {
                homeDishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP2_HOME_TEAM_TOTAL;
            } else if ("3".equals(key)) {
                homeDishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP3_HOME_TEAM_TOTAL;
            }
            if (homeDishName != null) {
                String homeDishId = dishMapping.get(homeDishName);
                if (homeDishId != null) {
                    // 0 进球数 2 大赔率 3 小赔率
                    List<Object> home = (List<Object>) totalKill.get(0);
                    if (!CollectionUtils.isEmpty(home)) {
                        // 进球数
                        String totalNum = (String) home.get(0);
                        // 大赔率
                        String bigOdds = (String) home.get(2);
                        // 小赔率
                        String smallOdds = (String) home.get(3);

                        PbEsports homePbEsports = new PbEsports();
                        BeanUtils.copyProperties(pbEsports, homePbEsports);
                        homePbEsports.setId(LangUtils.generateUuid());
                        homePbEsports.setDishId(homeDishId);
                        homePbEsports.setDishName(homeDishName);
                        homePbEsports.setHomeTeamOdds(bigOdds);
                        homePbEsports.setGuestTeamOdds(smallOdds);
                        homePbEsports.setHomeTeamItem(totalNum);
                        homePbEsports.setHomeExtraDishName(PBConstant.EXTRA_DISH_NAME_DP);
                        homePbEsports.setGuestExtraDishName(PBConstant.EXTRA_DISH_NAME_XP);
                        pbEsportsList.add(homePbEsports);
                    }
                }
            }

            // 2. 客队进球
            String guestDishName = null;
            if ("1".equals(key)) {
                guestDishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP1_GUEST_TEAM_TOTAL;
            } else if ("2".equals(key)) {
                guestDishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP2_GUEST_TEAM_TOTAL;
            } else if ("3".equals(key)) {
                guestDishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP3_GUEST_TEAM_TOTAL;
            }
            if (guestDishName != null) {
                String guestDishId = dishMapping.get(guestDishName);
                if (guestDishId != null) {
                    // 0 进球数 2 大赔率 3 小赔率
                    List<Object> guest = (List<Object>) totalKill.get(1);
                    if (!CollectionUtils.isEmpty(guest)) {
                        // 进球数
                        String totalNum = (String) guest.get(0);
                        // 大赔率
                        String bigOdds = (String) guest.get(2);
                        // 小赔率
                        String smallOdds = (String) guest.get(3);

                        PbEsports guestPbEsports = new PbEsports();
                        BeanUtils.copyProperties(pbEsports, guestPbEsports);
                        guestPbEsports.setId(LangUtils.generateUuid());
                        guestPbEsports.setDishId(guestDishId);
                        guestPbEsports.setDishName(guestDishName);
                        guestPbEsports.setHomeTeamOdds(bigOdds);
                        guestPbEsports.setGuestTeamOdds(smallOdds);
                        guestPbEsports.setHomeTeamItem(totalNum);
                        guestPbEsports.setHomeExtraDishName(PBConstant.EXTRA_DISH_NAME_DP);
                        guestPbEsports.setGuestExtraDishName(PBConstant.EXTRA_DISH_NAME_XP);
                        pbEsportsList.add(guestPbEsports);
                    }
                }
            }
        }
        return pbEsportsList;
    }

    /**
     * （击杀数）更多盘口的处理方式之2 - 让分盘
     */
    private List<PbEsports> moreKillDishHandler2(PbEsports pbEsports, String key, List<List<Object>> rfpList, Map<String, String> dishMapping) {
        List<PbEsports> pbEsportsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(rfpList)) {
            String dishName = null;
            if ("1".equals(key)) {
                dishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP1_RFP;
            } else if ("2".equals(key)) {
                dishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP2_RFP;
            } else if ("3".equals(key)) {
                dishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP3_RFP;
            }
            if (dishName != null) {
                String dishId = dishMapping.get(dishName);
                if (dishId != null) {
                    for (List<Object> rfp : rfpList) {
                        // 队伍1让球
                        BigDecimal homeTeamItem = (BigDecimal) rfp.get(1);
                        // 队伍2让球
                        BigDecimal guestTeamItem = (BigDecimal) rfp.get(0);
                        // 队1让球赔率
                        String homeTeamOdds = (String) rfp.get(3);
                        // 队2让球赔率
                        String guestTeamOdds = (String) rfp.get(4);

                        PbEsports rfpPbEsports = new PbEsports();
                        BeanUtils.copyProperties(pbEsports, rfpPbEsports);
                        rfpPbEsports.setId(LangUtils.generateUuid());
                        rfpPbEsports.setDishId(dishId);
                        rfpPbEsports.setDishName(dishName);
                        rfpPbEsports.setHomeTeamOdds(homeTeamOdds);
                        rfpPbEsports.setGuestTeamOdds(guestTeamOdds);
                        rfpPbEsports.setHomeTeamItem(homeTeamItem.toString());
                        rfpPbEsports.setGuestTeamItem(guestTeamItem.toString());
                        pbEsportsList.add(rfpPbEsports);
                    }
                }
            }
        }
        return pbEsportsList;
    }

    /**
     * （击杀数）更多盘口的处理方式之3 - 大小盘
     */
    private List<PbEsports> moreKillDishHandler3(PbEsports pbEsports, String key, List<List<Object>> dxpList, Map<String, String> dishMapping) {
        List<PbEsports> pbEsportsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dxpList)) {
            String dishName = null;
            if ("1".equals(key)) {
                dishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP1_DXP;
            } else if ("2".equals(key)) {
                dishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP2_DXP;
            } else if ("3".equals(key)) {
                dishName = PBConstant.CUSTOM_DISH_NAME_KILL_MAP3_DXP;
            }
            if (dishName != null) {
                String dishId = dishMapping.get(dishName);
                if (dishId != null) {
                    for (List<Object> dxp : dxpList) {
                        // 大小数
                        String dxItem = (String) dxp.get(0);
                        // 主队赔率
                        String homeTeamOdds = (String) dxp.get(2);
                        // 客队赔率
                        String guestTeamOdds = (String) dxp.get(3);

                        PbEsports dxpPbEsports = new PbEsports();
                        BeanUtils.copyProperties(pbEsports, dxpPbEsports);
                        dxpPbEsports.setId(LangUtils.generateUuid());
                        dxpPbEsports.setDishId(dishId);
                        dxpPbEsports.setDishName(dishName);
                        dxpPbEsports.setHomeTeamOdds(homeTeamOdds);
                        dxpPbEsports.setGuestTeamOdds(guestTeamOdds);
                        dxpPbEsports.setHomeTeamItem(dxItem);
                        dxpPbEsports.setHomeExtraDishName(PBConstant.EXTRA_DISH_NAME_DP);
                        dxpPbEsports.setGuestExtraDishName(PBConstant.EXTRA_DISH_NAME_XP);
                        pbEsportsList.add(dxpPbEsports);
                    }
                }
            }
        }
        return pbEsportsList;
    }

    /**
     * CS:GO - 更多
     */
    private void dealCsGoMore(PbEsports initPbEsports, Map<String, List<Object>> moreMap, int maxMap) {
        // 获取对应盘口字典表
        Map<String, String> dishMapping = Dictionary.getEsportDishMappingByTypeAndDishType(initPbEsports.getType(), Constant.ESPORTS_DISH_PB);

        List<PbEsports> pbEsportsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(moreMap)) {
            List<Object> e = moreMap.get("e");
            if (!CollectionUtils.isEmpty(e)) {
                List<Object> list = (List<Object>) e.get(3);
                if (!CollectionUtils.isEmpty(list)) {
                    Map<String, Object> moreDishMap = (Map<String, Object>) list.get(8);
                    if (!CollectionUtils.isEmpty(moreDishMap)) {
                        boolean dealSe = false;
                        for (String key : moreDishMap.keySet()) {
                            if (!doMap4Kill(key, moreDishMap)) {
                                continue;
                            }

                            // 0 球队总得分 2让分盘 3大小盘
                            List<Object> moreDishList = (List<Object>) moreDishMap.get(key);
                            if (!CollectionUtils.isEmpty(moreDishList)) {
                                // 1. 球队总得分     0主队 1客队
                                List<Object> totalKill = (List<Object>) moreDishList.get(0);
                                List<PbEsports> totalKillPbEsports = moreKillDishHandler1(initPbEsports, key, totalKill, dishMapping);
                                if (!CollectionUtils.isEmpty(totalKillPbEsports)) {
                                    pbEsportsList.addAll(totalKillPbEsports);
                                }

                                // 2. 让分盘
                                List<List<Object>> rfpList = (List<List<Object>>) moreDishList.get(2);
                                List<PbEsports> rfpPbEsports = moreKillDishHandler2(initPbEsports, key, rfpList, dishMapping);
                                if (!CollectionUtils.isEmpty(rfpPbEsports)) {
                                    pbEsportsList.addAll(rfpPbEsports);
                                }

                                // 3. 大小盘
                                List<List<Object>> dxpList = (List<List<Object>>) moreDishList.get(3);
                                List<PbEsports> dxpPbEsports = moreKillDishHandler3(initPbEsports, key, dxpList, dishMapping);
                                if (!CollectionUtils.isEmpty(dxpPbEsports)) {
                                    pbEsportsList.addAll(dxpPbEsports);
                                }

                                // 4. se   只要处理一次
                                if (!dealSe) {
                                    List<Object> dishDetailList = (List<Object>) moreDishList.get(1);
                                    if (!CollectionUtils.isEmpty(dishDetailList)) {
                                        List<Map<String, Object>> allSeList = new ArrayList<>();

                                        // 只取“队”
                                        Map<String, Object> dishDetailMap0 = (Map<String, Object>) dishDetailList.get(0);
                                        if (!CollectionUtils.isEmpty(dishDetailMap0)) {
                                            String cg = (String) dishDetailMap0.get("cg");
                                            if (cg.equals("队")) {
                                                List<Map<String, Object>> seList = (List<Map<String, Object>>) dishDetailMap0.get("se");
                                                if (!CollectionUtils.isEmpty(seList)) {
                                                    allSeList.addAll(seList);
                                                }
                                            } else {
                                                Map<String, Object> dishDetailMap1 = (Map<String, Object>) dishDetailList.get(1);
                                                if (!CollectionUtils.isEmpty(dishDetailMap1)) {
                                                    List<Map<String, Object>> seList = (List<Map<String, Object>>) dishDetailMap1.get("se");
                                                    if (!CollectionUtils.isEmpty(seList)) {
                                                        allSeList.addAll(seList);
                                                    }
                                                }
                                            }
                                        }

                                        if (!CollectionUtils.isEmpty(allSeList)) {
                                            for (Map<String, Object> se : allSeList) {
                                                // 盘口名
                                                String dishName = (String) se.get("n");
                                                String dishId = dishMapping.get(dishName);
                                                if (dishId == null) {
                                                    continue;
                                                }
                                                if (!doMap4More(maxMap, dishName)) {
                                                    continue;
                                                }
                                                // 具体赔率
                                                List<Map<String, Object>> oddsList = (List<Map<String, Object>>) se.get("l");
                                                if (CollectionUtils.isEmpty(oddsList)) {
                                                    continue;
                                                }

                                                PbEsports pbEsports = new PbEsports();
                                                BeanUtils.copyProperties(initPbEsports, pbEsports);
                                                pbEsports.setId(LangUtils.generateUuid());
                                                pbEsports.setDishId(dishId);
                                                pbEsports.setDishName(dishName);

                                                // 各个盘具体处理方式不太一样,只能一个个单独处理
                                                String dishType = Dictionary.ESPORT_DISH_TYPE_MAPPING.get(dishId);
                                                if (dishType == null) {
                                                    continue;
                                                }

                                                if (Constant.DISH_TYPE_SYP.equals(dishType)) {
                                                    // 队名判断
                                                    moreDishHandler1(pbEsports, pbEsports.getHomeTeamName(), oddsList);
                                                    pbEsportsList.add(pbEsports);
                                                } else if (Constant.DISH_TYPE_SFP.equals(dishType)) {
                                                    // 不是/是
                                                    moreDishHandler2(pbEsports, oddsList);
                                                    pbEsportsList.add(pbEsports);
                                                }  else if (Constant.DISH_TYPE_DSP.equals(dishType)) {
                                                    // 单双盘
                                                    moreDishHandler4(pbEsports, oddsList);
                                                }
                                            }
                                        }

                                        dealSe = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // 保存爬取的数据
        savePbEsports(pbEsportsList);
    }

    /**
     * 判断是不是（击杀数）盘
     * @param teamName
     * @return
     */
    private boolean isKill(String teamName) {
        return teamName.endsWith(PBConstant.TEAM_NAME_KILL_SUFFIX);
    }

    /**
     * bo1只爬全场; bo3只爬前两场; bo5只爬前三场
     */
    private boolean doMap(String key, Map<String, List<Object>> dishMap) {
        if (dishMap.containsKey("5")) {
            // bo5
            if ("4".equals(key) || "5".equals(key)) {
                return false;
            }
        } else if (dishMap.containsKey("3")) {
            // bo3
            if ("3".equals(key)) {
                return false;
            }
        } else if (!dishMap.containsKey("2") && dishMap.containsKey("1")) {
            // bo1
            if ("1".equals(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取爬取的最大地图数
     */
    private int getMaxMap(Map<String, List<Object>> dishMap) {
        if (dishMap.containsKey("5")) {
            // bo5
            return 3;
        } else if (dishMap.containsKey("3")) {
            // bo3
            return 2;
        } else if (dishMap.containsKey("2")) {
            // bo2
            return 2;
        }
        return 1;
    }

    /**
     * 爬虫的地图 - 更多
     */
    private boolean doMap4More(int maxMap, String dishName) {
        if (maxMap == 3) {
            if (!(dishName.contains("(地图 1)") || dishName.contains("(地图 2)") || dishName.contains("(地图 3)")
                || dishName.contains("(Map 1)") || dishName.contains("(Map 2)") || dishName.contains("(Map 3)"))) {
                return false;
            }
        } else if (maxMap == 2) {
            if (!(dishName.contains("(地图 1)") || dishName.contains("(地图 2)")
                    || dishName.contains("(Map 1)") || dishName.contains("(Map 2)"))) {
                return false;
            }
        }

        return true;
    }

    /**
     * bo1只爬第一场; bo3只爬前两场; bo5只爬前三场
     */
    private boolean doMap4Kill(String key, Map<String, Object> moreDishMap) {
        if (moreDishMap.containsKey("5")) {
            // bo5
            if ("4".equals(key) || "5".equals(key)) {
                return false;
            }
        } else if (moreDishMap.containsKey("3")) {
            // bo3
            if ("3".equals(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据顺序key获取对应输赢盘
     */
    private String getDishNameByKey(String key) {
        switch (key) {
            case "0":
                return PBConstant.CUSTOM_DISH_NAME_WHOLE_SYP;
            case "1":
                return PBConstant.CUSTOM_DISH_NAME_MAP1_SYP;
            case "2":
                return PBConstant.CUSTOM_DISH_NAME_MAP2_SYP;
            case "3":
                return PBConstant.CUSTOM_DISH_NAME_MAP3_SYP;
            default:
                return null;
        }
    }

    /**
     * 保存
     * @param pbEsports
     */
    private void savePbEsports(List<PbEsports> pbEsports) {
        if (!CollectionUtils.isEmpty(pbEsports)) {
            pbEsportsRepository.saveAll(pbEsports);
            pbEsportsRepository.flush();
        }
    }
}
