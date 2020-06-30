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

import java.util.*;

/**
 * IM电竞盘口 (废弃)
 */
@SuppressWarnings("all")
@Slf4j
@Service
public class ImESportsService implements BaseService {
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

        JSONObject body = getBaseBody(null, null);
        int retryCount = 0;
        while (true) {
            Map<String, Object> map = HttpClientUtils.post(IMConstant.IM_BASE_URL, body, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.ESPORTS_DISH_FY));
            if (map != null && map.get("d") != null) {
                List<List<Object>> d = (List<List<Object>>) map.get("d");
                if (!CollectionUtils.isEmpty(d)) {
                    try {
                        parseEsports(taskId, type, d, appointedLeagues, appointedTeams);
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

        long endTime = System.currentTimeMillis();
        log.info("IM电竞_" + type + "_" + taskId + "_[耗时（秒）: " + CommonUtils.getSeconds(endTime - startTime) + "]");
    }

    /**
     * 电竞解析
     * @param taskId
     * @param type
     * @param d
     */
    private void parseEsports(String taskId, String type, List<List<Object>> d, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
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
            for (List<Object> league : d) {
                // [8]
                Integer gameType = (Integer) league.get(8);
                if (!sportId.equals(gameType)) {
                    continue;
                }

                // [1][5]都有联赛信息,目前取[5]
                List<String> leagueNames = (List<String>) league.get(5);
                if (CollectionUtils.isEmpty(leagueNames) && leagueNames.size() < 2) {
                    continue;
                }
                // 联赛名
                String leagueName = leagueNames.get(1).trim();
                String leagueId = Dictionary.ESPORT_IM_LEAGUE_MAPPING.get(leagueName);
                if (leagueId == null) {
                    continue;
                }

                // 如果存在指定联赛, 进行过滤判断
                if (!FilterUtils.filterLeague(appointedLeagues, leagueId)) {
                    continue;
                }

                // [10]比赛列表
                List<List<Object>> games = (List<List<Object>>) league.get(10);
                if (!CollectionUtils.isEmpty(games)) {
                    for (List<Object> game : games) {
                        // [5]主队    [6]客队
                        List<String> homeTeamNames = (List<String>) game.get(5);
                        if (CollectionUtils.isEmpty(homeTeamNames) && homeTeamNames.size() < 2) {
                            continue;
                        }
                        // 主队名
                        String homeTeamName = homeTeamNames.get(2).trim();

                        List<String> guestTeamNames = (List<String>) game.get(6);
                        if (CollectionUtils.isEmpty(guestTeamNames) && guestTeamNames.size() < 2) {
                            continue;
                        }
                        // 客队名
                        String guestTeamName = guestTeamNames.get(2).trim();

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

                        // [35]滚球标志
                        Integer gameStatus = (Integer) game.get(35);
                        if (!IMConstant.GAME_STATUS.equals(gameStatus)) {
                            continue;
                        }

                        // [7]获取比赛时间
                        String startTimestamp = (String) game.get(7);
                        String startTime = getStartTime(startTimestamp);
                        // 只取到第二天的比赛
                        if (TimeUtils.getNextDayLastTime().compareTo(startTime) < 0) {
                            continue;
                        }

                        // [19]比赛ID, 查询详细盘口需要使用
                        String matchId = (String) game.get(19);
                        if (StringUtils.isEmpty(matchId)) {
                            continue;
                        }

                        // [33]轮数, 如BO5
                        String round = (String) game.get(33);

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
                            String url = String.format(IMConstant.IM_MORE_URL, matchId);
                            JSONObject body = getBaseBody(sportId, matchId);
                            Map<String, Object> map = HttpClientUtils.post(url, body, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.ESPORTS_DISH_FY));
                            if (map != null && map.get("d") != null) {
                                List<List<Object>> moreD = (List<List<Object>>) map.get("d");
                                if (!CollectionUtils.isEmpty(moreD)) {
                                    try {
                                        parseMatchDetail(moreD, initImEsports, round);
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

    /**
     * 解析比赛详细盘口
     */
    private void parseMatchDetail(List<List<Object>> moreD, ImEsports initImEsports, String round) {
        // 获取对应盘口字典表
        Map<String, String> dishMapping = Dictionary.getEsportDishMappingByTypeAndDishType(initImEsports.getType(), Constant.ESPORTS_DISH_IM);
        // 盘口显示名字典表
        Map<String, String> displayMapping = Dictionary.getImDishDisplayMappingByType(initImEsports.getType());

        // 详细盘口, 就一个
        List<Object> match = moreD.get(0);
        // [10] 各个盘口信息
        List<List<Object>> allDish = (List<List<Object>>) match.get(10);
        if (!CollectionUtils.isEmpty(allDish)) {
            List<ImEsports> imEsportsList = new ArrayList<>();
            for (List<Object> dish : allDish) {
                // [28] 盘口名
                String dishName = (String) dish.get(28);
                dishName = dishName.trim();
                if (IMConstant.SKIP_DISH.equalsIgnoreCase(dishName)) {
                    continue;
                }
                ImSpecialDishEnum imSpecialDishEnum = ImSpecialDishEnum.getImSpecialDishByOriginalValue(dishName);
                if (imSpecialDishEnum != null) {
                    dishName = imSpecialDishEnum.getCustomValue();
                }

                // [27] 局数
                Integer gameNo = (Integer) dish.get(27);
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

                // [10]盘口赔率信息, 取第一个
                List<List<Object>> oddsInfos = (List<List<Object>>) dish.get(10);
                if (!CollectionUtils.isEmpty(oddsInfos)) {
                    for (List<Object> oddsInfo : oddsInfos) {
                        if (CollectionUtils.isEmpty(oddsInfo)) {
                            continue;
                        }

                        // 初始化
                        ImEsports imEsports = new ImEsports();
                        BeanUtils.copyProperties(initImEsports, imEsports);
                        imEsports.setId(LangUtils.generateUuid());
                        imEsports.setDishId(dishId);
                        imEsports.setDishName(displayMapping.get(matchDishName));

                        // oddsInfo[6]赔率
                        List<Object> odds = (List<Object>) oddsInfo.get(6);
                        if  (odds == null) {
                            // 如果[6]没有取[7]
                            odds = (List<Object>) oddsInfo.get(7);
                        }
                        // odds[4]主队赔率
                        String homeTeamOdds = (String) odds.get(4);
                        // odds[7]客队赔率
                        String guestTeamOdds = (String) odds.get(7);

                        if (Constant.DISH_TYPE_SYP.equals(dishType)) {
                            // 输赢盘
                            imEsports.setHomeTeamOdds(homeTeamOdds);
                            imEsports.setGuestTeamOdds(guestTeamOdds);

                            imEsportsList.add(imEsports);
                        } else if (Constant.DISH_TYPE_RFP.equals(dishType)) {
                            // 让分盘
                            String homeTeamItem = null;
                            String guestTeamItem = null;
                            if (imSpecialDishEnum != null) {
                                homeTeamItem = imSpecialDishEnum.getHomeTeamItem();
                                guestTeamItem = imSpecialDishEnum.getGuestTeamItem();
                            } else {
                                String rfItem = (String) odds.get(2);
                                homeTeamItem = "-" + rfItem;
                                guestTeamItem = rfItem;
                            }
                            imEsports.setHomeTeamOdds(homeTeamOdds);
                            imEsports.setHomeTeamItem(homeTeamItem);
                            imEsports.setGuestTeamOdds(guestTeamOdds);
                            imEsports.setGuestTeamItem(guestTeamItem);

                            imEsportsList.add(imEsports);
                        } else if (Constant.DISH_TYPE_DXP.equals(dishType) || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                            // 大小盘
                            String dxItem = (String) odds.get(2);

                            imEsports.setHomeTeamOdds(homeTeamOdds);
                            imEsports.setHomeTeamItem(dxItem);
                            imEsports.setHomeExtraDishName(IMConstant.EXTRA_DISH_NAME_GREATER_THAN);

                            imEsports.setGuestTeamOdds(guestTeamOdds);
                            imEsports.setGuestExtraDishName(IMConstant.EXTRA_DISH_NAME_LESS_THAN);

                            imEsportsList.add(imEsports);
                        } else if (Constant.DISH_TYPE_DSP.equals(dishType)) {
                            // 单双盘
                            imEsports.setHomeTeamOdds(homeTeamOdds);
                            imEsports.setHomeExtraDishName(IMConstant.EXTRA_DISH_NAME_ODD);

                            imEsports.setGuestTeamOdds(guestTeamOdds);
                            imEsports.setGuestExtraDishName(IMConstant.EXTRA_DISH_NAME_EVEN);
                            imEsportsList.add(imEsports);
                        } else if (Constant.DISH_TYPE_SFP.equals(dishType)) {
                            // 是否盘
                            // 暂无
                        }
                    }
                }
            }
            // 保存
            saveImEsports(imEsportsList);
        }
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
     * 开赛时间转换, /Date(1587892500000)/
     * @param startTimestamp
     * @return
     */
    private String getStartTime(String startTimestamp) {
        int start = startTimestamp.indexOf("(");
        int end = startTimestamp.indexOf(")");
        String timstamp = startTimestamp.substring(start+1, end);
        return TimeUtils.format(Long.valueOf(timstamp));
    }

    /**
     * 获取请求body
     */
    private JSONObject getBaseBody(Integer sportId, String parentMatchNo) {
        JSONObject body = new JSONObject();
        body.put("WalletMode", 2);
        body.put("WalletBalanceDisplayEnabled", true);
        body.put("WalletBalanceRefreshInterval", 60);
        body.put("OddsType", 2);
        body.put("Language", 1);
        body.put("ShowStatistics", 0);
        body.put("ExtraFilter", "48");
        body.put("CompanyId", "1582");
        body.put("AcceptAnyOdds", false);
        body.put("AcceptHigherOdds", true);
        body.put("SeasonId", 0);
        body.put("VIPSpread", 1);
        body.put("Playsite", false);
        body.put("SportId", -1);    // 请求具体比赛时,需要替换 ("SportId": "45")
        if (sportId != null) {
            body.put("SportId", sportId);
        }
        body.put("Market", 0);
        body.put("OddsPageCode", 0);
        body.put("ShowStatsLeftFloatMenu", false);
        body.put("ShowMatchResults", true);
        body.put("ShowTeamLeftFloatMenu", true);
        body.put("ShowTermsLeftFloatMenu", true);
        body.put("ShowAnnouncementLeftFloatMenu", true);
        body.put("ShowCMS", false);
        body.put("showAnnouncement", true);
        body.put("TranslationCode", "REAL");
        body.put("IsMultipleCurrency", false);
        body.put("QueryToken", "");
        body.put("LiveStream", 1);
        body.put("MobileMerchantHomeUrl", null);
        body.put("IndexMatchesReloadIntervalSeconds", 20);
        body.put("LiveBallsReloadIntervalSeconds", 7);
        body.put("SmvReloadIntervalSeconds", 10);
        body.put("ParlayViewReloadIntervalSeconds", 10);
        body.put("NoSponsorAdsInVideo", false);
        body.put("FilterSportId", -1);
        body.put("PageSportIds", Collections.singletonList(sportId));
        body.put("PageMarket", 3);
        body.put("ViewType", 2);
        body.put("MemberCode", "");
        if (parentMatchNo != null) {
            body.put("ParentMatchNo", null);    // 请求具体比赛时,需要替换 ("ParentMatchNo": "9793752")
        }
        body.put("ParentMatchNo", parentMatchNo);
        body.put("MatchIdList", null);
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
