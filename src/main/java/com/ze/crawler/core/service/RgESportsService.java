package com.ze.crawler.core.service;

import com.alibaba.fastjson.JSON;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.RGConstant;
import com.ze.crawler.core.entity.RgEsports;
import com.ze.crawler.core.model.*;
import com.ze.crawler.core.repository.RgEsportsRepository;
import com.ze.crawler.core.service.log.LogService;
import com.ze.crawler.core.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * RG电竞盘口
 */
@Slf4j
@Service
public class RgESportsService implements BaseService {
    @Autowired
    private RgEsportsRepository rgEsportsRepository;
    @Autowired
    private LogService logService;

    /**
     * RG电竞爬虫
     * @param taskId
     * @param type
     * @param appointedLeagues  指定联赛
     * @param appointedTeams    指定队伍
     */
    @Override
    public void crawler(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        log.info("RG电竞_" + type + "_" + taskId);

        long startTime = System.currentTimeMillis();

        // 今日
        for (int page=1; page <= RGConstant.MAX_PAGE; page++) {
            int retryCount = 0;
            while (true) {
                String url = String.format(RGConstant.RG_BASE_URL, page, RGConstant.MATCH_TYPE_TODAY);
                RgESportsResultModel rgESportsResultModel = HttpClientUtils.get(url, RgESportsResultModel.class);
                if (rgESportsResultModel != null && !CollectionUtils.isEmpty(rgESportsResultModel.getResult())) {
                    try {
                        parseEsports(taskId, type, rgESportsResultModel.getResult(), false, appointedLeagues, appointedTeams);
                    } catch (Exception e) {
                        Map<String, String> data = new HashMap<>();
                        data.put("url", url);
                        data.put("result", JSON.toJSONString(rgESportsResultModel));
                        data.put("retry_count", String.valueOf(retryCount));
                        logService.log(Constant.LOG_TYPE_PARSE_ESPORTS_ERROR, Constant.ESPORTS_DISH_RG.toString(), JSON.toJSONString(data), e);
                    }
                    break;
                }

                retryCount++;
                if (retryCount >= Constant.RETRY_COUNT) {
                    break;
                }
            }
        }

        // 赛前。 包含未来几天的数据，只找第二天的
        for (int page=1; page <= RGConstant.MAX_PAGE; page++) {
            int retryCount = 0;
            while (true) {
                String url = String.format(RGConstant.RG_BASE_URL, page, RGConstant.MATCH_TYPE_ZP);
                RgESportsResultModel rgESportsResultModel = HttpClientUtils.get(url, RgESportsResultModel.class);
                if (rgESportsResultModel != null && !CollectionUtils.isEmpty(rgESportsResultModel.getResult())) {
                    try {
                        parseEsports(taskId, type, rgESportsResultModel.getResult(), true, appointedLeagues, appointedTeams);
                    } catch (Exception e) {
                        Map<String, String> data = new HashMap<>();
                        data.put("url", url);
                        data.put("result", JSON.toJSONString(rgESportsResultModel));
                        data.put("retry_count", String.valueOf(retryCount));
                        logService.log(Constant.LOG_TYPE_PARSE_ESPORTS_ERROR, Constant.ESPORTS_DISH_RG.toString(), JSON.toJSONString(data), e);
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
        log.info("RG电竞_" + type + "_" + taskId + "_[耗时（秒）: " + CommonUtils.getSeconds(endTime - startTime) + "]");
    }

    /**
     * 解析电竞
     * @param taskId
     * @param type
     * @param result
     * @param isZp  判断是不是早盘。 如果是早盘需要对开赛时间进行处理（只取第二天的）
     */
    private void parseEsports(String taskId, String type, List<RgESportsResultItemModel> result, boolean isZp, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        // 第二天最后时刻。早盘需要使用
        String nextDayLastTime = TimeUtils.getNextDayLastTime();

        for (RgESportsResultItemModel item : result) {
            // 赛事名
            String gameName = item.getGameName();

            // 根据type过滤想要的联赛
            if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_LOL)) {
                if (!gameName.equals(RGConstant.GAME_NAME_LOL)) {
                    continue;
                }
            } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_DOTA2)) {
                if (!gameName.equals(RGConstant.GAME_NAME_DOTA2)) {
                    continue;
                }
            } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_CSGO)) {
                if (!gameName.equals(RGConstant.GAME_NAME_CSGO)) {
                    continue;
                }
            } else {
                // 其余赛事, 比如王者荣耀, 暂不需要
                continue;
            }

            // 1为 未开始
            if (!RGConstant.RESULT_ITEM_STATUS_NORMAL.equals(item.getStatus())) {
                continue;
            }

            // 如果是早盘，只取第二天的数据
            if (isZp && nextDayLastTime.compareTo(item.getStartTime())<0) {
                continue;
            }

            // 赛事信息获取
            String leagueName = item.getTournamentName().trim();
            String leagueId = Dictionary.ESPORT_RG_LEAGUE_MAPPING.get(leagueName);
            if (leagueId == null) {
                continue;
            }

            // 如果存在指定联赛, 进行过滤判断
            if (!FilterUtils.filterLeague(appointedLeagues, leagueId)) {
                continue;
            }

            // 队伍信息
            Integer homeTeamId = null;
            String homeTeamName = null;
            Integer guestTeamId = null;
            String guestTeamName = null;
            if (!CollectionUtils.isEmpty(item.getTeam())) {
                // 主队
                RgESportsResultTeamModel homeTeam = item.getTeam().get(1);
                homeTeamId = homeTeam.getTeamId();
                homeTeamName = homeTeam.getTeamName();

                // 客队
                RgESportsResultTeamModel guestTeam = item.getTeam().get(0);
                guestTeamId = guestTeam.getTeamId();
                guestTeamName = guestTeam.getTeamName();
            }
            if (homeTeamId==null || homeTeamName==null || guestTeamId==null || guestTeamName==null) {
                continue;
            }
            homeTeamName = homeTeamName.trim();
            guestTeamName = guestTeamName.trim();

            // 获取主客队信息
            String dishHomeTeamId = Dictionary.ESPORT_RG_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
            String dishGuestTeamId = Dictionary.ESPORT_RG_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());
            if (dishHomeTeamId == null || dishGuestTeamId == null) {
                continue;
            }

            // 如果存在指定队伍, 进行过滤判断
            if (!FilterUtils.filterTeam(appointedTeams, dishHomeTeamId, dishGuestTeamId)) {
                continue;
            }

            // 获取对应盘口字典表
            Map<String, String> dishMapping = Dictionary.getEsportDishMappingByTypeAndDishType(type, Constant.ESPORTS_DISH_RG);

            // 获取更多赔率
            int retryCount = 0;
            while (true) {
                String url = String.format(RGConstant.RG_MORE_URL, item.getId());
                RgESportsOddsResultModel rgESportsOddsResultModel = HttpClientUtils.get(url, RgESportsOddsResultModel.class);
                if (rgESportsOddsResultModel != null && rgESportsOddsResultModel.getResult() != null) {
                    // 局数
                    String round = rgESportsOddsResultModel.getResult().getRound();

                    List<RgESportsResultItemOddsModel> oddsList = rgESportsOddsResultModel.getResult().getOdds();
                    if (!CollectionUtils.isEmpty(oddsList)) {
                        // 待存储数据
                        List<RgEsports> rgEsportsList = new ArrayList<>();

                        // 初始化一个, 避免重复赋值
                        RgEsports initRgEsports = new RgEsports();
                        initRgEsports.setTaskId(taskId);
                        initRgEsports.setType(type);
                        initRgEsports.setLeagueId(leagueId);
                        initRgEsports.setLeagueName(leagueName);
                        initRgEsports.setHomeTeamId(dishHomeTeamId);
                        initRgEsports.setHomeTeamName(homeTeamName);
                        initRgEsports.setGuestTeamId(dishGuestTeamId);
                        initRgEsports.setGuestTeamName(guestTeamName);
                        initRgEsports.setStartTime(item.getStartTime());

                        // 存储已处理的oddsId
                        Set<Integer> doneOddsIds = new HashSet<>();
                        for (RgESportsResultItemOddsModel odds : oddsList) {
                            String groupName = odds.getGroupName().trim();
                            String matchStage = odds.getMatchStage();
                            Integer oddsGroupId = odds.getOddsGroupId();
                            Integer oddsId = odds.getOddsId();
                            Integer teamId = odds.getTeamId();

                            // 已处理,跳过
                            if (doneOddsIds.contains(oddsId)) {
                                continue;
                            }
                            // status为1时在前台才显示
                            if (!RGConstant.RESULT_ITEM_ODDS_STATUS_NORMAL.equals(odds.getStatus())) {
                                continue;
                            }

                            // 获取对应盘口
                            String matchStageName = getMatchStage(matchStage, round);
                            if (matchStageName == null) {
                                continue;
                            }
                            String dishName = matchStageName + groupName;
                            String dishId = dishMapping.get(dishName);
                            if (dishId == null) {
                                continue;
                            }
                            RgEsports rgEsports = new RgEsports();
                            BeanUtils.copyProperties(initRgEsports, rgEsports);
                            rgEsports.setId(LangUtils.generateUuid());
                            rgEsports.setDishId(dishId);
                            rgEsports.setDishName(dishName);

                            // 各个盘具体处理方式不太一样,只能一个个单独处理
                            String dishType = Dictionary.ESPORT_DISH_TYPE_MAPPING.get(dishId);
                            if (dishType == null) {
                                continue;
                            }

                            Integer rpOddsId = null;
                            if (Constant.DISH_TYPE_SYP.equals(dishType)) {
                                // 输赢盘
                                if (homeTeamId.equals(teamId)) {
                                    rgEsports.setHomeTeamOdds(odds.getOdds());
                                } else {
                                    rgEsports.setGuestTeamOdds(odds.getOdds());
                                }

                                // 找对手盘
                                RgESportsResultItemOddsModel rivalPlate = getRivalPlate(oddsList, oddsGroupId, oddsId);
                                if (rivalPlate != null) {
                                    if (homeTeamId.equals(rivalPlate.getTeamId())) {
                                        rgEsports.setHomeTeamOdds(rivalPlate.getOdds());
                                    } else {
                                        rgEsports.setGuestTeamOdds(rivalPlate.getOdds());
                                    }

                                    rgEsportsList.add(rgEsports);
                                    rpOddsId = rivalPlate.getOddsId();
                                }
                            } else if (Constant.DISH_TYPE_RFP.equals(dishType)) {
                                // 让分盘
                                String value = getValue(odds.getValue());
                                String matchValue = getMatchValue(odds.getValue());
                                if (homeTeamId.equals(teamId)) {
                                    rgEsports.setHomeTeamOdds(odds.getOdds());
                                    rgEsports.setHomeTeamItem(value);
                                } else {
                                    rgEsports.setGuestTeamOdds(odds.getOdds());
                                    rgEsports.setGuestTeamItem(value);
                                }
                                // 找对手盘
                                RgESportsResultItemOddsModel rivalPlate = getRivalPlate(oddsList, oddsGroupId, oddsId, matchValue, odds.getValue(), teamId);
                                if (rivalPlate != null) {
                                    String rpValue = getValue(rivalPlate.getValue());
                                    if (homeTeamId.equals(rivalPlate.getTeamId())) {
                                        rgEsports.setHomeTeamOdds(rivalPlate.getOdds());
                                        rgEsports.setHomeTeamItem(rpValue);
                                    } else {
                                        rgEsports.setGuestTeamOdds(rivalPlate.getOdds());
                                        rgEsports.setGuestTeamItem(rpValue);
                                    }

                                    rgEsportsList.add(rgEsports);
                                    rpOddsId = rivalPlate.getOddsId();
                                }
                            } else if (Constant.DISH_TYPE_DXP.equals(dishType) || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                                // 大小盘
                                String value = getValue(odds.getValue());
                                if (Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                                    value = value + ".0";
                                }
                                String matchValue = getMatchValue(odds.getValue());

                                String name = odds.getName();
                                if (name.startsWith(RGConstant.EXTRA_DISH_NAME_GREATER_THAN)) {
                                    rgEsports.setHomeTeamOdds(odds.getOdds());
                                    rgEsports.setHomeTeamItem(value);
                                    rgEsports.setHomeExtraDishName(RGConstant.EXTRA_DISH_NAME_GREATER_THAN);
                                } else {
                                    rgEsports.setGuestTeamOdds(odds.getOdds());
                                    rgEsports.setGuestExtraDishName(RGConstant.EXTRA_DISH_NAME_LESS_THAN);
                                }

                                // 找对手盘
                                RgESportsResultItemOddsModel rivalPlate = getRivalPlate(oddsList, oddsGroupId, oddsId, matchValue, odds.getValue());
                                if (rivalPlate != null) {
                                    String rivalPlateName = rivalPlate.getName();
                                    if (rivalPlateName.startsWith(RGConstant.EXTRA_DISH_NAME_GREATER_THAN)) {
                                        rgEsports.setHomeTeamOdds(rivalPlate.getOdds());
                                        rgEsports.setHomeTeamItem(value);
                                        rgEsports.setHomeExtraDishName(RGConstant.EXTRA_DISH_NAME_GREATER_THAN);
                                    } else {
                                        rgEsports.setGuestTeamOdds(rivalPlate.getOdds());
                                        rgEsports.setGuestExtraDishName(RGConstant.EXTRA_DISH_NAME_LESS_THAN);
                                    }

                                    rgEsportsList.add(rgEsports);
                                    rpOddsId = rivalPlate.getOddsId();
                                }
                            } else if (Constant.DISH_TYPE_DSP.equals(dishType)) {
                                // 单双盘
                                if ("odd".equalsIgnoreCase(odds.getValue())) {
                                    rgEsports.setHomeTeamOdds(odds.getOdds());
                                    rgEsports.setHomeExtraDishName(RGConstant.EXTRA_DISH_NAME_ODD);
                                } else {
                                    rgEsports.setGuestTeamOdds(odds.getOdds());
                                    rgEsports.setGuestExtraDishName(RGConstant.EXTRA_DISH_NAME_EVEN);
                                }

                                // 找对手盘
                                RgESportsResultItemOddsModel rivalPlate = getRivalPlate(oddsList, oddsGroupId, oddsId);
                                if (rivalPlate != null) {
                                    if ("odd".equalsIgnoreCase(rivalPlate.getValue())) {
                                        rgEsports.setHomeTeamOdds(rivalPlate.getOdds());
                                        rgEsports.setHomeExtraDishName(RGConstant.EXTRA_DISH_NAME_ODD);
                                    } else {
                                        rgEsports.setGuestTeamOdds(rivalPlate.getOdds());
                                        rgEsports.setGuestExtraDishName(RGConstant.EXTRA_DISH_NAME_EVEN);
                                    }

                                    rgEsportsList.add(rgEsports);
                                    rpOddsId = rivalPlate.getOddsId();
                                }
                            } else if (Constant.DISH_TYPE_SFP.equals(dishType)) {
                                // 是否盘
                                if ("No".equalsIgnoreCase(odds.getValue())) {
                                    rgEsports.setHomeTeamOdds(odds.getOdds());
                                    rgEsports.setHomeExtraDishName(odds.getName());
                                } else {
                                    rgEsports.setGuestTeamOdds(odds.getOdds());
                                    rgEsports.setGuestExtraDishName(odds.getName());
                                }

                                // 找对手盘
                                RgESportsResultItemOddsModel rivalPlate = getRivalPlate(oddsList, oddsGroupId, oddsId);
                                if (rivalPlate != null) {
                                    if ("No".equalsIgnoreCase(rivalPlate.getValue())) {
                                        rgEsports.setHomeTeamOdds(rivalPlate.getOdds());
                                        rgEsports.setHomeExtraDishName(rivalPlate.getName());
                                    } else {
                                        rgEsports.setGuestTeamOdds(rivalPlate.getOdds());
                                        rgEsports.setGuestExtraDishName(rivalPlate.getName());
                                    }

                                    rgEsportsList.add(rgEsports);
                                    rpOddsId = rivalPlate.getOddsId();
                                }
                            }

                            // 表示oddsId已处理
                            doneOddsIds.add(oddsId);
                            if (rpOddsId != null) {
                                doneOddsIds.add(rpOddsId);
                            }
                        }

                        // 保存
                        saveRgEsports(rgEsportsList);
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

    /**
     * 找对手盘
     */
    private RgESportsResultItemOddsModel getRivalPlate(List<RgESportsResultItemOddsModel> oddsList, Integer oddsGroupId, Integer oddsId) {
        for (RgESportsResultItemOddsModel rivalPlate : oddsList) {
            if (rivalPlate.getOddsGroupId().equals(oddsGroupId) && !rivalPlate.getOddsId().equals(oddsId)) {
                return rivalPlate;
            }
        }
        return null;
    }

    /**
     * 找对手盘 - 让分盘使用
     */
    private RgESportsResultItemOddsModel getRivalPlate(List<RgESportsResultItemOddsModel> oddsList, Integer oddsGroupId, Integer oddsId, String matchValue, String value, Integer teamId) {
        for (RgESportsResultItemOddsModel rivalPlate : oddsList) {
            String rpMatchValue = getMatchValue(rivalPlate.getValue());
            if (rivalPlate.getOddsGroupId().equals(oddsGroupId)
                    && !rivalPlate.getOddsId().equals(oddsId)
                    && matchValue.equals(rpMatchValue)
                    && !rivalPlate.getValue().equals(value)
                    && !rivalPlate.getTeamId().equals(teamId)) {
                return rivalPlate;
            }
        }
        return null;
    }

    /**
     * 找对手盘 - 大小盘使用
     */
    private RgESportsResultItemOddsModel getRivalPlate(List<RgESportsResultItemOddsModel> oddsList, Integer oddsGroupId, Integer oddsId, String matchValue, String value) {
        for (RgESportsResultItemOddsModel rivalPlate : oddsList) {
            String rpMatchValue = getMatchValue(rivalPlate.getValue());
            if (rivalPlate.getOddsGroupId().equals(oddsGroupId)
                    && !rivalPlate.getOddsId().equals(oddsId)
                    && matchValue.equals(rpMatchValue)
                    && !rivalPlate.getValue().equals(value)) {
                return rivalPlate;
            }
        }
        return null;
    }

    /**
     * value特殊处理
     * @param value
     * @return
     */
    private String getValue(String value) {
        if (value.startsWith("+")) {
            return value.substring(1);
        } else if (value.startsWith(">")) {
            return value.substring(1);
        } else if (value.startsWith("<")) {
            return value.substring(1);
        }
        return value;
    }

    /**
     * value特殊处理 - 匹配使用
     * @param value
     * @return
     */
    private String getMatchValue(String value) {
        if (value.startsWith("+")) {
            return value.substring(1);
        } else if (value.startsWith("-")) {
            return value.substring(1);
        } else if (value.startsWith(">")) {
            return value.substring(1);
        } else if (value.startsWith("<")) {
            return value.substring(1);
        }
        return value;
    }

    /**
     * 获取比赛地图
     */
    private String getMatchStage(String matchStage, String round) {
        if (RGConstant.MATCH_STAGE_FINAL.equalsIgnoreCase(matchStage)) {
            // 全场
            return "全场_";
        } else if (RGConstant.MATCH_STAGE_R1.equalsIgnoreCase(matchStage) || RGConstant.MATCH_STAGE_MAP1.equalsIgnoreCase(matchStage)) {
            // 第一局
            return "第一局_";
        } else if (RGConstant.MATCH_STAGE_R2.equalsIgnoreCase(matchStage) || RGConstant.MATCH_STAGE_MAP2.equalsIgnoreCase(matchStage)) {
            // 第二局
            return "第二局_";
        } else if (RGConstant.MATCH_STAGE_R3.equalsIgnoreCase(matchStage) || RGConstant.MATCH_STAGE_MAP3.equalsIgnoreCase(matchStage)) {
            // 第三局
            if (RGConstant.ROUND_BO3.equalsIgnoreCase(round)) {
                return null;
            }
            return "第三局_";
        }
        return null;
    }

    /**
     * 保存
     * @param rgEsports
     */
    private void saveRgEsports(List<RgEsports> rgEsports) {
        if (!CollectionUtils.isEmpty(rgEsports)) {
            rgEsportsRepository.saveAll(rgEsports);
            rgEsportsRepository.flush();
        }
    }
}
