package com.ze.crawler.core.service;

import com.alibaba.fastjson.JSON;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.TFConstant;
import com.ze.crawler.core.entity.TfEsports;
import com.ze.crawler.core.model.*;
import com.ze.crawler.core.repository.TfEsportsRepository;
import com.ze.crawler.core.service.log.LogService;
import com.ze.crawler.core.utils.HttpClientUtils;
import com.ze.crawler.core.utils.LangUtils;
import com.ze.crawler.core.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TF电竞盘口
 */
@SuppressWarnings("all")
@Slf4j
@Service
public class TfESportsService implements ESportsService {
    @Autowired
    private TfEsportsRepository tfEsportsRepository;
    @Autowired
    private LogService logService;

    // 认证token
    private final static String AUTHORIZATION = "Token c4b789e82ce341ac985e44b6b4da5042";

    /**
     * TF电竞爬虫
     * @param taskId
     * @param type  赛事类型，LOL、DOTA2、CSGO
     */
    @Override
    public void crawler(String taskId, String type) {
        log.info("TF电竞_" + type + "_" + taskId);

        Integer gameId = null;
        if (Constant.ESPORTS_TYPE_LOL.equalsIgnoreCase(type)) {
            gameId = TFConstant.GAME_ID_LOL;
        } else if (Constant.ESPORTS_TYPE_DOTA2.equalsIgnoreCase(type)) {
            gameId = TFConstant.GAME_ID_DOTA2;
        } else if (Constant.ESPORTS_TYPE_CSGO.equalsIgnoreCase(type)) {
            gameId = TFConstant.GAME_ID_CSGO;
        }

        if (gameId != null) {
            // 今日
            int retryCount = 0;
            while (true) {
                String url = String.format(TFConstant.TF_TODAY_URL, gameId);
                List tfESportsResultModels = HttpClientUtils.get(url, List.class, AUTHORIZATION);
                if (!CollectionUtils.isEmpty(tfESportsResultModels)) {
                    try {
                        parseEsports(taskId, type, tfESportsResultModels);
                    } catch (Exception e) {
                        Map<String, String> data = new HashMap<>();
                        data.put("url", url);
                        data.put("result", JSON.toJSONString(tfESportsResultModels));
                        data.put("retry_count", String.valueOf(retryCount));
                        logService.log(Constant.LOG_TYPE_PARSE_ESPORTS_ERROR, Constant.ESPORTS_DISH_TF.toString(), JSON.toJSONString(data), e);
                    }
                    break;
                }

                retryCount++;
                if (retryCount >= Constant.RETRY_COUNT) {
                    break;
                }
            }

            // 早盘
            retryCount = 0;
            while (true) {
                String url = String.format(TFConstant.TF_ZP_URL, gameId, TimeUtils.getNextDay());
                List tfESportsResultModels = HttpClientUtils.get(url, List.class, AUTHORIZATION);
                if (!CollectionUtils.isEmpty(tfESportsResultModels)) {
                    try {
                        parseEsports(taskId, type, tfESportsResultModels);
                    } catch (Exception e) {
                        Map<String, String> data = new HashMap<>();
                        data.put("url", url);
                        data.put("result", JSON.toJSONString(tfESportsResultModels));
                        data.put("retry_count", String.valueOf(retryCount));
                        logService.log(Constant.LOG_TYPE_PARSE_ESPORTS_ERROR, Constant.ESPORTS_DISH_TF.toString(), JSON.toJSONString(data), e);
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
     * 解析电竞
     * @param taskId
     * @param type
     * @param result
     */
    private void parseEsports(String taskId, String type, List result) {
        // 获取对应盘口字典表
        Map<String, String> dishMapping = Dictionary.getEsportDishMappingByTypeAndDishType(type, Constant.ESPORTS_DISH_TF);

        for (Object object : result) {
            TfESportsResultModel tfESportsResultModel = JSON.parseObject(JSON.toJSONString(object), TfESportsResultModel.class);
            // 联赛名
            String leagueName = tfESportsResultModel.getCompetitionName().trim();
            if (!Dictionary.ESPORT_TF_LEAGUE_MAPPING.containsKey(leagueName)) {
                logService.log(Constant.LOG_TYPE_LEAGUE_NOT_FOUND, Constant.ESPORTS_DISH_TF.toString(), type + ":" + leagueName);
                continue;
            }
            String leagueId = Dictionary.ESPORT_TF_LEAGUE_MAPPING.get(leagueName);
            if (leagueId == null) {
                continue;
            }

            // 主客队信息
            String homeTeamName = null;
            String guestTeamName = null;
            if (tfESportsResultModel.getHome() != null) {
                homeTeamName = tfESportsResultModel.getHome().getTeamName();
            }
            if (tfESportsResultModel.getAway() != null) {
                guestTeamName = tfESportsResultModel.getAway().getTeamName();
            }
            if (homeTeamName==null || guestTeamName==null) {
                continue;
            }
            // 获取主客队信息
            String homeTeamId = Dictionary.ESPORT_TF_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
            String guestTeamId = Dictionary.ESPORT_TF_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());
            if (homeTeamId == null || guestTeamId == null) {
                if (homeTeamId == null) {
                    logService.log(Constant.LOG_TYPE_TEAM_NOT_FOUND, Constant.ESPORTS_DISH_TF.toString(), type + ":" + leagueId + "#" + homeTeamName);
                }
                if (guestTeamId == null) {
                    logService.log(Constant.LOG_TYPE_TEAM_NOT_FOUND, Constant.ESPORTS_DISH_TF.toString(), type + ":" + leagueId + "#" + guestTeamName);
                }
                continue;
            }

            // 比赛id
            Integer eventId = tfESportsResultModel.getEventId();
            // 比赛开始时间
            String startTime = tfESportsResultModel.getStartDatetime();
            // 轮数
            String round = tfESportsResultModel.getBestOf();

            // 初始化一个, 避免重复赋值
            TfEsports initTfEsports = new TfEsports();
            initTfEsports.setTaskId(taskId);
            initTfEsports.setType(type);
            initTfEsports.setLeagueId(leagueId);
            initTfEsports.setLeagueName(leagueName);
            initTfEsports.setHomeTeamId(homeTeamId);
            initTfEsports.setHomeTeamName(homeTeamName);
            initTfEsports.setGuestTeamId(guestTeamId);
            initTfEsports.setGuestTeamName(guestTeamName);
            initTfEsports.setStartTime(startTime);

            // 获取[总局]对应赔率
            if (!CollectionUtils.isEmpty(tfESportsResultModel.getMarkets())) {
                List<TfEsports> tfEsportsList = new ArrayList<>();

                for (TfESportsResultMarketModel market : tfESportsResultModel.getMarkets()) {
                    String dishName = market.getMarketName();
                    String dishId = dishMapping.get(dishName);
                    if (dishId != null) {
                        String dishType = Dictionary.ESPORT_DISH_TYPE_MAPPING.get(dishId);
                        if (dishType == null) {
                            continue;
                        }

                        if (!CollectionUtils.isEmpty(market.getSelection())) {
                            TfEsports tfEsports = new TfEsports();
                            BeanUtils.copyProperties(initTfEsports, tfEsports);
                            tfEsports.setId(LangUtils.generateUuid());
                            tfEsports.setDishId(dishId);
                            tfEsports.setDishName(dishName);

                            boolean open = true;
                            for (TfESportsResultMarketSelectionModel selection : market.getSelection()) {
                                // 前台不可见
                                if (!TFConstant.MARKET_SELECTION_STATUS_OPEN.equalsIgnoreCase(selection.getStatus())) {
                                    open = false;
                                    break;
                                }

                                if (Constant.DISH_TYPE_SYP.equals(dishType)) {
                                    // 独赢
                                    if (TFConstant.TEAM_HOME.equalsIgnoreCase(selection.getName())) {
                                        tfEsports.setHomeTeamOdds(selection.getEuroOdds().toString());
                                    } else {
                                        tfEsports.setGuestTeamOdds(selection.getEuroOdds().toString());
                                    }
                                } else if (Constant.DISH_TYPE_RFP.equals(dishType)) {
                                    // 让分局
                                    String value = getValue(selection.getHandicap());
                                    if (TFConstant.TEAM_HOME.equalsIgnoreCase(selection.getName())) {
                                        tfEsports.setHomeTeamOdds(selection.getEuroOdds().toString());
                                        tfEsports.setHomeTeamItem(value);
                                    } else {
                                        tfEsports.setGuestTeamOdds(selection.getEuroOdds().toString());
                                        tfEsports.setGuestTeamItem(value);
                                    }
                                } else if (Constant.DISH_TYPE_DXP.equals(dishType) || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                                    // 大小盘
                                    if (TFConstant.DXP_OVER.equalsIgnoreCase(selection.getName())) {
                                        tfEsports.setHomeTeamOdds(selection.getEuroOdds().toString());
                                        String dxItem = selection.getHandicap();
                                        if (Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                                            dxItem = dxItem + ".0";
                                        }
                                        tfEsports.setHomeTeamItem(dxItem);
                                        tfEsports.setHomeExtraDishName(selection.getBetTypeSelectionName());
                                    } else {
                                        tfEsports.setGuestTeamOdds(selection.getEuroOdds().toString());
                                        tfEsports.setGuestExtraDishName(selection.getBetTypeSelectionName());
                                    }
                                }
                            }

                            if (open) {
                                tfEsportsList.add(tfEsports);
                            }
                        }
                    }
                }
                // 保存
                saveTfEsports(tfEsportsList);
            }

            // 获取[各局]对应赔率
            if (!CollectionUtils.isEmpty(tfESportsResultModel.getMarketTabs())) {
                for (TfESportsResultMarketTabModel marketTab : tfESportsResultModel.getMarketTabs()) {
                    // 在今日或早盘中已经带有总局的赔率信息,没有必要再请求一次,可以过滤掉
                    if (TFConstant.TAB_NAME_MATCH.equalsIgnoreCase(marketTab.getTabName())) {
                        continue;
                    }
                    if (!doMap(marketTab.getTabName(), round)) {
                        continue;
                    }

                    int retryCount = 0;
                    while (true) {
                        String url = String.format(TFConstant.TF_MAP_URL, eventId, marketTab.getTabName().replace(" ", "%20"));
                        List tfESportsResultModels = HttpClientUtils.get(url, List.class, AUTHORIZATION);
                        if (!CollectionUtils.isEmpty(tfESportsResultModels)) {
                            try {
                                TfESportsResultModel mapResult = JSON.parseObject(JSON.toJSONString(tfESportsResultModels.get(0)), TfESportsResultModel.class);
                                if (!CollectionUtils.isEmpty(mapResult.getMarkets())) {
                                    List<TfEsports> tfEsportsList = new ArrayList<>();

                                    for (TfESportsResultMarketModel market : mapResult.getMarkets()) {
                                        String dishName = market.getMarketName();
                                        String dishId = dishMapping.get(dishName);
                                        if (dishId != null) {
                                            String dishType = Dictionary.ESPORT_DISH_TYPE_MAPPING.get(dishId);
                                            if (dishType == null) {
                                                continue;
                                            }
                                            if (!CollectionUtils.isEmpty(market.getSelection())) {
                                                TfEsports tfEsports = new TfEsports();
                                                BeanUtils.copyProperties(initTfEsports, tfEsports);
                                                tfEsports.setId(LangUtils.generateUuid());
                                                tfEsports.setDishId(dishId);
                                                tfEsports.setDishName(dishName);

                                                boolean open = true;
                                                for (TfESportsResultMarketSelectionModel selection : market.getSelection()) {
                                                    // 前台不可见
                                                    if (!TFConstant.MARKET_SELECTION_STATUS_OPEN.equalsIgnoreCase(selection.getStatus())) {
                                                        open = false;
                                                        break;
                                                    }

                                                    if (Constant.DISH_TYPE_SYP.equals(dishType)) {
                                                        // 输赢盘
                                                        if (TFConstant.TEAM_HOME.equalsIgnoreCase(selection.getName())) {
                                                            tfEsports.setHomeTeamOdds(selection.getEuroOdds().toString());
                                                        } else {
                                                            tfEsports.setGuestTeamOdds(selection.getEuroOdds().toString());
                                                        }
                                                    } else if (Constant.DISH_TYPE_RFP.equals(dishType)) {
                                                        // 让分盘
                                                        String value = getValue(selection.getHandicap());
                                                        if (TFConstant.TEAM_HOME.equalsIgnoreCase(selection.getName())) {
                                                            tfEsports.setHomeTeamOdds(selection.getEuroOdds().toString());
                                                            tfEsports.setHomeTeamItem(value);
                                                        } else {
                                                            tfEsports.setGuestTeamOdds(selection.getEuroOdds().toString());
                                                            tfEsports.setGuestTeamItem(value);
                                                        }
                                                    } else if (Constant.DISH_TYPE_DXP.equals(dishType) || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                                                        // 大小盘
                                                        if (TFConstant.DXP_OVER.equalsIgnoreCase(selection.getName())) {
                                                            tfEsports.setHomeTeamOdds(selection.getEuroOdds().toString());
                                                            String dxItem = selection.getHandicap();
                                                            if (Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                                                                dxItem = dxItem + ".0";
                                                            }
                                                            tfEsports.setHomeTeamItem(dxItem);
                                                            tfEsports.setHomeExtraDishName(selection.getBetTypeSelectionName());
                                                        } else {
                                                            tfEsports.setGuestTeamOdds(selection.getEuroOdds().toString());
                                                            tfEsports.setGuestExtraDishName(selection.getBetTypeSelectionName());
                                                        }
                                                    } else if (Constant.DISH_TYPE_DSP.equals(dishType)) {
                                                        // 单双盘
                                                        if (TFConstant.DSP_ODD.equalsIgnoreCase(selection.getName())) {
                                                            tfEsports.setHomeTeamOdds(selection.getEuroOdds().toString());
                                                            tfEsports.setHomeExtraDishName(selection.getBetTypeSelectionName());
                                                        } else {
                                                            tfEsports.setGuestTeamOdds(selection.getEuroOdds().toString());
                                                            tfEsports.setGuestExtraDishName(selection.getBetTypeSelectionName());
                                                        }
                                                    } else if (Constant.DISH_TYPE_SFP.equals(dishType)) {
                                                        // 是否盘
                                                        if (TFConstant.SFP_NO.equalsIgnoreCase(selection.getName())) {
                                                            tfEsports.setHomeTeamOdds(selection.getEuroOdds().toString());
                                                            tfEsports.setHomeExtraDishName(selection.getBetTypeSelectionName());
                                                        } else {
                                                            tfEsports.setGuestTeamOdds(selection.getEuroOdds().toString());
                                                            tfEsports.setGuestExtraDishName(selection.getBetTypeSelectionName());
                                                        }
                                                    }
                                                }

                                                if (open) {
                                                    tfEsportsList.add(tfEsports);
                                                }
                                            }
                                        }
                                    }
                                    // 保存
                                    saveTfEsports(tfEsportsList);
                                }
                            } catch (Exception e) {
                                Map<String, String> data = new HashMap<>();
                                data.put("url", url);
                                data.put("result", JSON.toJSONString(tfESportsResultModels));
                                data.put("retry_count", String.valueOf(retryCount));
                                logService.log(Constant.LOG_TYPE_PARSE_ESPORTS_ERROR, Constant.ESPORTS_DISH_TF.toString(), JSON.toJSONString(data), e);
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
     * value特殊处理
     * @param value
     * @return
     */
    private String getValue(String value) {
        if (value.startsWith("+")) {
            return value.substring(1);
        }
        return value;
    }

    /**
     * 过滤场次
     */
    private boolean doMap(String tabName, String round) {
        if (TFConstant.ROUND_BO5.equals(round)) {
            // bo5
            if (tabName.equals(TFConstant.TAB_NAME_MAP4) || tabName.equals(TFConstant.TAB_NAME_MAP5)) {
                return false;
            }
        } else if (TFConstant.ROUND_BO3.equals(round)) {
            // bo3
            if (tabName.equals(TFConstant.TAB_NAME_MAP3)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 保存
     * @param tfEsports
     */
    private void saveTfEsports(List<TfEsports> tfEsports) {
        if (!CollectionUtils.isEmpty(tfEsports)) {
            tfEsportsRepository.saveAll(tfEsports);
            tfEsportsRepository.flush();
        }
    }
}
