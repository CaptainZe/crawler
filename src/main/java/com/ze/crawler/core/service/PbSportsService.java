package com.ze.crawler.core.service;

import com.alibaba.fastjson.JSON;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.PBConstant;
import com.ze.crawler.core.constants.ProxyConstant;
import com.ze.crawler.core.entity.PbSports;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.PbSportsRepository;
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
 * 平博盘口 - 体育
 */
@SuppressWarnings("all")
@Slf4j
@Service
public class PbSportsService implements BaseService {
    @Autowired
    private PbSportsRepository pbSportsRepository;
    @Autowired
    private LogService logService;

    @Override
    public void crawler(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        log.info("平博体育_" + type + "_" + taskId);
        long startTime = System.currentTimeMillis();

        String sp = null;
        if (type.equalsIgnoreCase(Constant.SPORTS_TYPE_BASKETBALL)) {
            sp = PBConstant.SP_BASKETBALL;
        } else if (type.equalsIgnoreCase(Constant.SPORTS_TYPE_SOCCER)) {
            sp = PBConstant.SP_SOCCER;
        }
        if (sp == null) {
            return;
        }

        int retryCount = 0;
        while (true) {
            // 今天
            String url = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_TODAY, sp, "", System.currentTimeMillis());
            Map<String, Object> map = HttpClientUtils.get(url, Map.class, ProxyConstant.USE_PROXY);
            if (map != null && map.get("n") != null && !CollectionUtils.isEmpty((List<Object>) map.get("n"))) {
                try {
                    parseSports(taskId, type, map, appointedLeagues, appointedTeams);
                } catch (Exception e) {
                    Map<String, String> data = new HashMap<>();
                    data.put("url", url);
                    data.put("result", JSON.toJSONString(map));
                    data.put("retry_count", String.valueOf(retryCount));
                    logService.log(Constant.LOG_TYPE_PARSE_SPORTS_ERROR, Constant.SPORTS_DISH_PB.toString(), JSON.toJSONString(data), e);
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
            String url = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_ZP, sp, TimeUtils.getDate(), System.currentTimeMillis());
            Map<String, Object> map = HttpClientUtils.get(url, Map.class, ProxyConstant.USE_PROXY);
            if (map != null && map.get("n") != null && !CollectionUtils.isEmpty((List<Object>) map.get("n"))) {
                try {
                    parseSports(taskId, type, map, appointedLeagues, appointedTeams);
                } catch (Exception e) {
                    Map<String, String> data = new HashMap<>();
                    data.put("url", url);
                    data.put("result", JSON.toJSONString(map));
                    data.put("retry_count", String.valueOf(retryCount));
                    logService.log(Constant.LOG_TYPE_PARSE_SPORTS_ERROR, Constant.SPORTS_DISH_PB.toString(), JSON.toJSONString(data), e);
                }
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("平博体育_" + type + "_" + taskId + "_[耗时（秒）: " + CommonUtils.getSeconds(endTime - startTime) + "]");
    }

    /**
     * 体育解析
     */
    private void parseSports(String taskId, String type, Map<String, Object> map, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        if (map != null) {
            List<Object> n = (List<Object>) map.get("n");
            if (!CollectionUtils.isEmpty(n)) {
                // sports 体育盘列表 (1是Basketball/Soccer 2是联赛列表)
                List<Object> sports = (List<Object>) n.get(0);
                if (!CollectionUtils.isEmpty(sports)) {
                    // 联赛列表
                    List<List<Object>> leagues = (List<List<Object>>) sports.get(2);
                    if (!CollectionUtils.isEmpty(leagues)) {
                        // 遍历联赛列表
                        for (List<Object> league : leagues) {
                            // 联赛名
                            String leagueName = ((String) league.get(1)).trim();

                            // 赛事信息获取
                            String leagueId = Dictionary.SPORT_PB_LEAGUE_MAPPING.get(leagueName);
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

                                    // 获取主客队信息
                                    String homeTeamId = Dictionary.SPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName);
                                    String guestTeamId = Dictionary.SPORT_PB_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName);
                                    if (homeTeamId == null || guestTeamId == null) {
                                        continue;
                                    }

                                    // 如果存在指定队伍, 进行过滤判断
                                    if (!FilterUtils.filterTeam(appointedTeams, homeTeamId, guestTeamId)) {
                                        continue;
                                    }

                                    // 初始化一个, 避免重复赋值
                                    PbSports initPbSports = new PbSports();
                                    initPbSports.setTaskId(taskId);
                                    initPbSports.setType(type);
                                    initPbSports.setLeagueId(leagueId);
                                    initPbSports.setLeagueName(leagueName);
                                    initPbSports.setHomeTeamId(homeTeamId);
                                    initPbSports.setHomeTeamName(homeTeamName);
                                    initPbSports.setGuestTeamId(guestTeamId);
                                    initPbSports.setGuestTeamName(guestTeamName);
                                    initPbSports.setStartTime(startTime);

                                    // 具体赔率等信息. (KEY: 0表示全场 1表示上半场)
                                    Map<String, List<Object>> dishMap = (Map<String, List<Object>>) game.get(8);
                                    dealSports(meParam, initPbSports, dishMap);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理各个盘口
     * @param dishMap       具体赔率等信息. (KEY: 0表示全场 1表示上半场)
     */
    private void dealSports(Integer meParam, PbSports initPbSports, Map<String, List<Object>> dishMap){
        // 获取对应盘口字典表
        Map<String, String> dishMapping = Dictionary.getSportDishMappingByTypeAndDishType(initPbSports.getType(), Constant.SPORTS_DISH_PB);

        List<PbSports> pbSportsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dishMap)) {
            for (String key : dishMap.keySet()) {
                // 0表示让分盘 1表示大小盘 2表示独赢
                List<Object> dishDetailInfos = dishMap.get(key);

                // 1.让分盘    可能有多个数据
                List<List<Object>> oddsInfos4Rfp = (List<List<Object>>) dishDetailInfos.get(0);
                if (!CollectionUtils.isEmpty(oddsInfos4Rfp)) {
                    // 自定义盘口名
                    String dishName = null;
                    if (key.equals("0")) {
                        dishName = PBConstant.CUSTOM_DISH_NAME_FULL_RFP;
                    } else if (key.equals("1")) {
                        dishName = PBConstant.CUSTOM_DISH_NAME_FIRST_HALF_RFP;
                    }
                    if (dishName != null) {
                        String dishId = dishMapping.get(dishName);
                        if (dishId != null) {
                            for (List<Object> oddsInfo : oddsInfos4Rfp) {
                                if (!CollectionUtils.isEmpty(oddsInfo)) {
                                    // 主队让分数
                                    BigDecimal homeTeamItem = (BigDecimal) oddsInfo.get(1);
                                    // 客队让分数
                                    BigDecimal guestTeamItem = (BigDecimal) oddsInfo.get(0);
                                    // 主队赔率
                                    String homeTeamOdds = (String) oddsInfo.get(3);
                                    // 客队赔率
                                    String guestTeamOdds = (String) oddsInfo.get(4);

                                    PbSports pbSports = new PbSports();
                                    BeanUtils.copyProperties(initPbSports, pbSports);
                                    pbSports.setId(LangUtils.generateUuid());
                                    pbSports.setDishId(dishId);
                                    pbSports.setDishName(dishName);
                                    pbSports.setHomeTeamOdds(homeTeamOdds);
                                    pbSports.setGuestTeamOdds(guestTeamOdds);
                                    pbSports.setHomeTeamItem(homeTeamItem.toString());
                                    pbSports.setGuestTeamItem(guestTeamItem.toString());
                                    pbSportsList.add(pbSports);
                                }
                            }
                        }
                    }
                }

                // 2.大小盘    可能有多个数据
                List<List<Object>> oddsInfos4Dxp = (List<List<Object>>) dishDetailInfos.get(1);
                if (!CollectionUtils.isEmpty(oddsInfos4Dxp)) {
                    // 自定义盘口名
                    String dishName = null;
                    if (key.equals("0")) {
                        dishName = PBConstant.CUSTOM_DISH_NAME_FULL_DXP;
                    } else if (key.equals("1")) {
                        dishName = PBConstant.CUSTOM_DISH_NAME_FIRST_HALF_DXP;
                    }
                    if (dishName != null) {
                        String dishId = dishMapping.get(dishName);
                        if (dishId != null) {
                            for (List<Object> oddsInfo : oddsInfos4Dxp) {
                                if (!CollectionUtils.isEmpty(oddsInfo)) {
                                    // 大小数
                                    BigDecimal dxItem = (BigDecimal) oddsInfo.get(1);
                                    // 主队赔率
                                    String homeTeamOdds = (String) oddsInfo.get(2);
                                    // 客队赔率
                                    String guestTeamOdds = (String) oddsInfo.get(3);

                                    PbSports pbSports = new PbSports();
                                    BeanUtils.copyProperties(initPbSports, pbSports);
                                    pbSports.setId(LangUtils.generateUuid());
                                    pbSports.setDishId(dishId);
                                    pbSports.setDishName(dishName);
                                    pbSports.setHomeTeamOdds(homeTeamOdds);
                                    pbSports.setGuestTeamOdds(guestTeamOdds);
                                    pbSports.setHomeTeamItem(dxItem.toString());
                                    pbSports.setHomeExtraDishName(PBConstant.EXTRA_DISH_NAME_DP);
                                    pbSports.setGuestExtraDishName(PBConstant.EXTRA_DISH_NAME_XP);
                                    pbSportsList.add(pbSports);
                                }
                            }
                        }
                    }
                }

                // 3.独赢     只有篮球全场需要爬取独赢盘
                if (initPbSports.getType().equalsIgnoreCase(Constant.SPORTS_TYPE_BASKETBALL) && key.equals("0")) {
                    List<Object> oddsInfos4Syp = (List<Object>) dishDetailInfos.get(2);
                    if (!CollectionUtils.isEmpty(oddsInfos4Syp)) {
                        // 自定义盘口名
                        String dishName = null;
                        if (key.equals("0")) {
                            dishName = PBConstant.CUSTOM_DISH_NAME_FULL_SYP;
                        } else if (key.equals("1")) {
                            dishName = PBConstant.CUSTOM_DISH_NAME_FIRST_HALF_SYP;
                        }
                        if (dishName != null) {
                            String dishId = dishMapping.get(dishName);
                            if (dishId != null) {
                                // 主队赔率
                                String homeTeamOdds = (String) oddsInfos4Syp.get(1);
                                // 客队赔率
                                String guestTeamOdds = (String) oddsInfos4Syp.get(0);

                                PbSports pbSports = new PbSports();
                                BeanUtils.copyProperties(initPbSports, pbSports);
                                pbSports.setId(LangUtils.generateUuid());
                                pbSports.setDishId(dishId);
                                pbSports.setDishName(dishName);
                                pbSports.setHomeTeamOdds(homeTeamOdds);
                                pbSports.setGuestTeamOdds(guestTeamOdds);
                                pbSportsList.add(pbSports);
                            }
                        }
                    }
                }
            }
        }
        // 保存
        savePbSports(pbSportsList);

        // 获取更多盘数据
        int retryCount = 0;
        while (true) {
            String moreUrl = String.format(PBConstant.PB_MORE_URL, PBConstant.MK_MORE, meParam, System.currentTimeMillis());
            Map<String, List<Object>> moreMap = HttpClientUtils.get(moreUrl, Map.class, ProxyConstant.USE_PROXY);
            if (moreMap != null && moreMap.get("e") != null && !CollectionUtils.isEmpty((List<Object>) moreMap.get("e"))) {
                try {
                    dealSportsMore(initPbSports, moreMap);
                } catch (Exception e) {
                    Map<String, String> data = new HashMap<>();
                    data.put("url", moreUrl);
                    data.put("result", JSON.toJSONString(moreMap));
                    data.put("retry_count", String.valueOf(retryCount));
                    logService.log(Constant.LOG_TYPE_PARSE_SPORTS_ERROR, Constant.SPORTS_DISH_PB.toString(), JSON.toJSONString(data), e);
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
     * 处理各个盘口 - 更多
     * @param moreMap
     */
    private void dealSportsMore(PbSports initPbSports, Map<String, List<Object>> moreMap) {
        // 获取对应盘口字典表
        Map<String, String> dishMapping = Dictionary.getSportDishMappingByTypeAndDishType(initPbSports.getType(), Constant.SPORTS_DISH_PB);

        List<PbSports> pbSportsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(moreMap)) {
            List<Object> e = moreMap.get("e");
            if (!CollectionUtils.isEmpty(e)) {
                List<Object> list = (List<Object>) e.get(3);
                if (!CollectionUtils.isEmpty(list)) {
                    // KEY: 0表示全场 1表示上半场
                    Map<String, Object> moreDishMap = (Map<String, Object>) list.get(8);
                    if (!CollectionUtils.isEmpty(moreDishMap)) {
                        // 初始化一个, 后续不需要重复赋值
                        PbSports pbSports = new PbSports();
                        BeanUtils.copyProperties(initPbSports, pbSports);

                        for (String key : moreDishMap.keySet()) {
                            // 0球队总得分 2让分盘 3大小盘
                            List<Object> moreDishList = (List<Object>) moreDishMap.get(key);
                            if (!CollectionUtils.isEmpty(moreDishList)) {
                                // 1. 球队总得分     0主队 1客队
                                List<Object> totalGoal = (List<Object>) moreDishList.get(0);
                                List<PbSports> totalGoalPbSports = moreDishHandler1(pbSports, key, totalGoal, dishMapping);
                                if (!CollectionUtils.isEmpty(totalGoalPbSports)) {
                                    pbSportsList.addAll(totalGoalPbSports);
                                }

                                // 2. 让分盘
                                List<List<Object>> rfpList = (List<List<Object>>) moreDishList.get(2);
                                List<PbSports> rfpPbSports = moreDishHandler2(pbSports, key, rfpList, dishMapping);
                                if (!CollectionUtils.isEmpty(rfpPbSports)) {
                                    pbSportsList.addAll(rfpPbSports);
                                }

                                // 3. 大小盘
                                List<List<Object>> dxpList = (List<List<Object>>) moreDishList.get(3);
                                List<PbSports> dxpPbSports = moreDishHandler3(pbSports, key, dxpList, dishMapping);
                                if (!CollectionUtils.isEmpty(dxpPbSports)) {
                                    pbSportsList.addAll(dxpPbSports);
                                }
                            }
                        }
                    }
                }
            }
        }
        // 保存
        savePbSports(pbSportsList);
    }

    /**
     * 更多盘处理方式1 - 球队总得分
     */
    private List<PbSports> moreDishHandler1(PbSports pbSports, String key, List<Object> totalGoal, Map<String, String> dishMapping) {
        List<PbSports> pbSportsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(totalGoal)) {
            // 1. 主队进球
            String homeDishName = null;
            if ("0".equals(key)) {
                homeDishName = PBConstant.CUSTOM_DISH_NAME_FULL_HOME_TEAM_TOTAL;
            } else if ("1".equals(key)) {
                homeDishName = PBConstant.CUSTOM_DISH_NAME_FIRST_HALF_HOME_TEAM_TOTAL;
            }
            if (homeDishName != null) {
                String homeDishId = dishMapping.get(homeDishName);
                if (homeDishId != null) {
                    // 0 进球数 2 大赔率 3 小赔率
                    List<Object> home = (List<Object>) totalGoal.get(0);
                    if (!CollectionUtils.isEmpty(home)) {
                        // 进球数
                        String totalNum = (String) home.get(0);
                        // 大赔率
                        String bigOdds = (String) home.get(2);
                        // 小赔率
                        String smallOdds = (String) home.get(3);

                        PbSports homePbSports = new PbSports();
                        BeanUtils.copyProperties(pbSports, homePbSports);
                        homePbSports.setId(LangUtils.generateUuid());
                        homePbSports.setDishId(homeDishId);
                        homePbSports.setDishName(homeDishName);
                        homePbSports.setHomeTeamOdds(bigOdds);
                        homePbSports.setGuestTeamOdds(smallOdds);
                        homePbSports.setHomeTeamItem(totalNum);
                        homePbSports.setHomeExtraDishName(PBConstant.EXTRA_DISH_NAME_DP);
                        homePbSports.setGuestExtraDishName(PBConstant.EXTRA_DISH_NAME_XP);
                        pbSportsList.add(homePbSports);
                    }
                }
            }

            // 2. 客队进球
            String guestDishName = null;
            if ("0".equals(key)) {
                guestDishName = PBConstant.CUSTOM_DISH_NAME_FULL_GUEST_TEAM_TOTAL;
            } else if ("1".equals(key)) {
                guestDishName = PBConstant.CUSTOM_DISH_NAME_FIRST_HALF_GUEST_TEAM_TOTAL;
            }
            if (guestDishName != null) {
                String guestDishId = dishMapping.get(guestDishName);
                if (guestDishId != null) {
                    // 0 进球数 2 大赔率 3 小赔率
                    List<Object> guest = (List<Object>) totalGoal.get(1);
                    if (!CollectionUtils.isEmpty(guest)) {
                        // 进球数
                        String totalNum = (String) guest.get(0);
                        // 大赔率
                        String bigOdds = (String) guest.get(2);
                        // 小赔率
                        String smallOdds = (String) guest.get(3);

                        PbSports homePbSports = new PbSports();
                        BeanUtils.copyProperties(pbSports, homePbSports);
                        homePbSports.setId(LangUtils.generateUuid());
                        homePbSports.setDishId(guestDishId);
                        homePbSports.setDishName(guestDishName);
                        homePbSports.setHomeTeamOdds(bigOdds);
                        homePbSports.setGuestTeamOdds(smallOdds);
                        homePbSports.setHomeTeamItem(totalNum);
                        homePbSports.setHomeExtraDishName(PBConstant.EXTRA_DISH_NAME_DP);
                        homePbSports.setGuestExtraDishName(PBConstant.EXTRA_DISH_NAME_XP);
                        pbSportsList.add(homePbSports);
                    }
                }
            }
        }
        return pbSportsList;
    }

    /**
     * 更多盘处理方式1 - 让分盘
     */
    private List<PbSports> moreDishHandler2(PbSports pbSports, String key, List<List<Object>> rfpList, Map<String, String> dishMapping) {
        List<PbSports> pbSportsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(rfpList)) {
            String dishName = null;
            if ("0".equals(key)) {
                dishName = PBConstant.CUSTOM_DISH_NAME_FULL_RFP;
            } else if ("1".equals(key)) {
                dishName = PBConstant.CUSTOM_DISH_NAME_FIRST_HALF_RFP;
            }
            if (dishName != null) {
                String dishId = dishMapping.get(dishName);
                if (dishId != null) {
                    for (List<Object> rfp : rfpList) {
                        // 主队让球
                        BigDecimal homeTeamItem = (BigDecimal) rfp.get(1);
                        // 客队让球
                        BigDecimal guestTeamItem = (BigDecimal) rfp.get(0);
                        // 主队让球赔率
                        String homeTeamOdds = (String) rfp.get(3);
                        // 客队让球赔率
                        String guestTeamOdds = (String) rfp.get(4);

                        PbSports rfpPbSports = new PbSports();
                        BeanUtils.copyProperties(pbSports, rfpPbSports);
                        rfpPbSports.setId(LangUtils.generateUuid());
                        rfpPbSports.setDishId(dishId);
                        rfpPbSports.setDishName(dishName);
                        rfpPbSports.setHomeTeamOdds(homeTeamOdds);
                        rfpPbSports.setGuestTeamOdds(guestTeamOdds);
                        rfpPbSports.setHomeTeamItem(homeTeamItem.toString());
                        rfpPbSports.setGuestTeamItem(guestTeamItem.toString());
                        pbSportsList.add(rfpPbSports);
                    }
                }
            }
        }
        return pbSportsList;
    }

    /**
     * 更多盘处理方式1 - 大小盘
     */
    private List<PbSports> moreDishHandler3(PbSports pbSports, String key, List<List<Object>> dxpList, Map<String, String> dishMapping) {
        List<PbSports> pbSportsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dxpList)) {
            String dishName = null;
            if ("0".equals(key)) {
                dishName = PBConstant.CUSTOM_DISH_NAME_FULL_DXP;
            } else if ("1".equals(key)) {
                dishName = PBConstant.CUSTOM_DISH_NAME_FIRST_HALF_DXP;
            }
            if (dishName != null) {
                String dishId = dishMapping.get(dishName);
                if (dishId != null) {
                    for (List<Object> dxp : dxpList) {
                        // 大小数
                        BigDecimal dxItem = (BigDecimal) dxp.get(1);
                        // 主队赔率
                        String homeTeamOdds = (String) dxp.get(2);
                        // 客队赔率
                        String guestTeamOdds = (String) dxp.get(3);

                        PbSports dxpPbSports = new PbSports();
                        BeanUtils.copyProperties(pbSports, dxpPbSports);
                        dxpPbSports.setId(LangUtils.generateUuid());
                        dxpPbSports.setDishId(dishId);
                        dxpPbSports.setDishName(dishName);
                        dxpPbSports.setHomeTeamOdds(homeTeamOdds);
                        dxpPbSports.setGuestTeamOdds(guestTeamOdds);
                        dxpPbSports.setHomeTeamItem(dxItem.toString());
                        dxpPbSports.setHomeExtraDishName(PBConstant.EXTRA_DISH_NAME_DP);
                        dxpPbSports.setGuestExtraDishName(PBConstant.EXTRA_DISH_NAME_XP);
                        pbSportsList.add(dxpPbSports);
                    }
                }
            }
        }
        return pbSportsList;
    }

    /**
     * 保存
     * @param pbSports
     */
    private void savePbSports(List<PbSports> pbSports) {
        if (!CollectionUtils.isEmpty(pbSports)) {
            pbSportsRepository.saveAll(pbSports);
            pbSportsRepository.flush();
        }
    }
}
