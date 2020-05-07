package com.ze.crawler.core.service.water;

import com.alibaba.fastjson.JSON;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.PBConstant;
import com.ze.crawler.core.entity.Sports;
import com.ze.crawler.core.entity.WaterYield;
import com.ze.crawler.core.repository.WaterYieldRepository;
import com.ze.crawler.core.service.display.DisplayService;
import com.ze.crawler.core.service.wk.WeiKongService;
import com.ze.crawler.core.utils.CommonUtils;
import com.ze.crawler.core.utils.LangUtils;
import com.ze.crawler.core.utils.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 水量计算器
 */
@SuppressWarnings("all")
@Service
public class WaterCalculator {

    @Autowired
    private DisplayService displayService;
    @Autowired
    private WaterYieldRepository waterYieldRepository;
    @Autowired
    private WeiKongService weiKongService;

    /**
     * 计算水量
     */
    public void calculateWater(Map<Integer, List<? extends Sports>> sportsMap, double threshold, Integer main, Integer sendType) {
        Set<Integer> alreadyDoMain = new HashSet<>();

        if (main == null) {
            // 两两比较
            for (Integer mainKey : sportsMap.keySet()) {
                alreadyDoMain.add(mainKey);

                List<Sports> mianSportsList = (List<Sports>) sportsMap.get(mainKey);
                if (!CollectionUtils.isEmpty(mianSportsList)) {
                    for (Integer rp : sportsMap.keySet()) {
                        if (alreadyDoMain.contains(rp)) {
                            continue;
                        }

                        List<Sports> rpSportsList = (List<Sports>) sportsMap.get(rp);
                        if (!CollectionUtils.isEmpty(rpSportsList)) {
                            List<WaterYield> waterYieldList = new ArrayList<>();
                            for (Sports mainSports : mianSportsList) {
                                for (Sports rpSports : rpSportsList) {
                                    // 匹配对手盘
                                    List<WaterYield> waterYields = matchRivalPlateAndCalculateAndDisplay(mainKey, mainSports, rp, rpSports, threshold);
                                    if (!CollectionUtils.isEmpty(waterYields)) {
                                        waterYieldList.addAll(waterYields);
                                    }
                                }
                            }
                            // 保存
                            saveWaterYield(waterYieldList);
                            // fixme 后期不用微信推送
                            informWater(waterYieldList, sendType);
                        }
                    }
                }
            }
        } else {
            // 指定主盘口
            List<Sports> mianSportsList = (List<Sports>) sportsMap.get(main);
            if (!CollectionUtils.isEmpty(mianSportsList)) {
                for (Integer rp : sportsMap.keySet()) {
                    if (rp.equals(main)) {
                        continue;
                    }

                    List<Sports> rpSportsList = (List<Sports>) sportsMap.get(rp);
                    if (!CollectionUtils.isEmpty(rpSportsList)) {
                        List<WaterYield> waterYieldList = new ArrayList<>();
                        for (Sports mainSports : mianSportsList) {
                            for (Sports rpSports : rpSportsList) {
                                // 匹配对手盘
                                List<WaterYield> waterYields = matchRivalPlateAndCalculateAndDisplay(main, mainSports, rp, rpSports, threshold);
                                if (!CollectionUtils.isEmpty(waterYields)) {
                                    waterYieldList.addAll(waterYields);
                                }
                            }
                        }
                        // 保存
                        saveWaterYield(waterYieldList);
                        // fixme 后期不用微信推送
                        informWater(waterYieldList, sendType);
                    }
                }
            }
        }
    }

    /**
     * 匹配对手盘 & 计算水量 & 报水存储
     */
    private List<WaterYield> matchRivalPlateAndCalculateAndDisplay(Integer mainDish, Sports mainSports, Integer rpDish, Sports rpSports, double threshold) {
        List<WaterYield> waterYields = new ArrayList<>();

        // 基础判断
        if (rpSports.getType().equals(mainSports.getType())
                && rpSports.getDishId().equals(mainSports.getDishId())
                && rpSports.getLeagueId().equals(mainSports.getLeagueId())) {

            // 盘口类型
            String dishType = Dictionary.ESPORT_DISH_TYPE_MAPPING.get(mainSports.getDishId());

            // 由于各个盘口的主客队定义不一样，只要两队一样，就认为是同一场比赛
            if (rpSports.getHomeTeamId().equals(mainSports.getHomeTeamId())
                    && rpSports.getGuestTeamId().equals(mainSports.getGuestTeamId())) {
                // 对手盘的主客队与主盘口一致
                boolean match = false;
                if (Constant.DISH_TYPE_SYP.equals(dishType)
                        || Constant.DISH_TYPE_DSP.equals(dishType)
                        || Constant.DISH_TYPE_SFP.equals(dishType)) {
                    match = true;
                } else if (Constant.DISH_TYPE_RFP.equals(dishType)
                        || Constant.DISH_TYPE_DXP.equals(dishType)
                        || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                    // 让分盘需要判断让分是否一致；大小盘需要判断大小数是否一致
                    if (mainSports.getHomeTeamItem().equals(rpSports.getHomeTeamItem())) {
                        match = true;
                    }
                }

                if (match) {
                    // 都要计算两次
                    String waterYield1 = CommonUtils.calculateWaterYield(mainSports.getHomeTeamOdds(), rpSports.getGuestTeamOdds());
                    if (controlWaterYield(waterYield1, threshold)) {
                        String display = displayService.display(dishType, waterYield1, mainDish, mainSports, true, rpDish, rpSports, true);
                        waterYields.add(getWaterYield(waterYield1, mainDish, mainSports, true, rpDish, rpSports, true, display));
                    }
                    String waterYield2 = CommonUtils.calculateWaterYield(mainSports.getGuestTeamOdds(), rpSports.getHomeTeamOdds());
                    if (controlWaterYield(waterYield2, threshold)) {
                        String display = displayService.display(dishType, waterYield2, mainDish, mainSports, false, rpDish, rpSports, false);
                        waterYields.add(getWaterYield(waterYield2, mainDish, mainSports, false, rpDish, rpSports, false, display));
                    }
                }
            } else if (rpSports.getGuestTeamId().equals(mainSports.getHomeTeamId())
                        && rpSports.getHomeTeamId().equals(mainSports.getGuestTeamId())) {
                // 对手盘的主客队与主盘口不一致
                boolean match = false;
                if (Constant.DISH_TYPE_SYP.equals(dishType)
                        || Constant.DISH_TYPE_DSP.equals(dishType)
                        || Constant.DISH_TYPE_SFP.equals(dishType)) {
                    match = true;
                } else if (Constant.DISH_TYPE_RFP.equals(dishType)) {
                    // 让分盘需要判断让分是否一致
                    if (mainSports.getHomeTeamItem().equals(rpSports.getGuestTeamItem())) {
                        match = true;
                    }
                } else if (Constant.DISH_TYPE_DXP.equals(dishType)
                            || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                    // 大小盘需要判断大小数是否一致
                    if (mainSports.getHomeTeamItem().equals(rpSports.getHomeTeamItem())) {
                        match = true;
                    }

                    // 特殊判断（目前电竞会遇到）
                    // 球队总得分_主/客队进球，在主客队定义不一致时不能使用
                    if (mainSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP1_HOME_TEAM_TOTAL)
                            || mainSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP1_GUEST_TEAM_TOTAL)
                            || mainSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP2_HOME_TEAM_TOTAL)
                            || mainSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP2_GUEST_TEAM_TOTAL)
                            || mainSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP3_HOME_TEAM_TOTAL)
                            || mainSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP3_GUEST_TEAM_TOTAL)
                            || rpSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP1_HOME_TEAM_TOTAL)
                            || rpSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP1_GUEST_TEAM_TOTAL)
                            || rpSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP2_HOME_TEAM_TOTAL)
                            || rpSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP2_GUEST_TEAM_TOTAL)
                            || rpSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP3_HOME_TEAM_TOTAL)
                            || rpSports.getDishName().equals(PBConstant.CUSTOM_DISH_NAME_KILL_MAP3_GUEST_TEAM_TOTAL)
                    ) {
                        match = false;
                    }
                }

                if (match) {
                    // 都要计算两次
                    if (Constant.DISH_TYPE_DSP.equals(dishType)
                        || Constant.DISH_TYPE_SFP.equals(dishType)
                        || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)
                        || Constant.DISH_TYPE_DXP.equals(dishType)) {
                        // 都要计算两次
                        String waterYield1 = CommonUtils.calculateWaterYield(mainSports.getHomeTeamOdds(), rpSports.getGuestTeamOdds());
                        if (controlWaterYield(waterYield1, threshold)) {
                            String display = displayService.display(dishType, waterYield1, mainDish, mainSports, true, rpDish, rpSports, true);
                            waterYields.add(getWaterYield(waterYield1, mainDish, mainSports, true, rpDish, rpSports, true, display));
                        }
                        String waterYield2 = CommonUtils.calculateWaterYield(mainSports.getGuestTeamOdds(), rpSports.getHomeTeamOdds());
                        if (controlWaterYield(waterYield2, threshold)) {
                            String display = displayService.display(dishType, waterYield2, mainDish, mainSports, false, rpDish, rpSports, false);
                            waterYields.add(getWaterYield(waterYield2, mainDish, mainSports, false, rpDish, rpSports, false, display));
                        }
                    } else {
                        // 需要反转
                        String waterYield1 = CommonUtils.calculateWaterYield(mainSports.getHomeTeamOdds(), rpSports.getHomeTeamOdds());
                        if (controlWaterYield(waterYield1, threshold)) {
                            String display = displayService.display(dishType, waterYield1, mainDish, mainSports, true, rpDish, rpSports, false);
                            waterYields.add(getWaterYield(waterYield1, mainDish, mainSports, true, rpDish, rpSports, false, display));
                        }
                        String waterYield2 = CommonUtils.calculateWaterYield(mainSports.getGuestTeamOdds(), rpSports.getGuestTeamOdds());
                        if (controlWaterYield(waterYield2, threshold)) {
                            String display = displayService.display(dishType, waterYield2, mainDish, mainSports, false, rpDish, rpSports, true);
                            waterYields.add(getWaterYield(waterYield2, mainDish, mainSports, false, rpDish, rpSports, true, display));
                        }
                    }
                }
            }
        }
        return waterYields;
    }

    /**
     * 水量控制
     * @param waterYield
     * @param threshold
     * @return
     */
    private boolean controlWaterYield(String waterYield, double threshold) {
        double d = Double.parseDouble(waterYield);
        return d >= threshold;
    }

    /**
     * 获取报水存储数据
     */
    private WaterYield getWaterYield(String waterYield, Integer mainDish, Sports mainSports, boolean home,
                                     Integer rpDish, Sports rpSports, boolean guest, String display) {
        WaterYield water = new WaterYield();
        water.setId(LangUtils.generateUuid());
        water.setTaskId(mainSports.getTaskId());
        water.setType(mainSports.getType());
        water.setMainDish(mainDish);
        water.setRpDish(rpDish);
        water.setDishId(mainSports.getDishId());
        water.setDishName(mainSports.getDishName());
        water.setLeagueId(mainSports.getLeagueId());
        water.setLeagueName(mainSports.getLeagueName());
        water.setHomeTeamId(mainSports.getHomeTeamId());
        water.setHomeTeamName(mainSports.getHomeTeamName());
        water.setGuestTeamId(mainSports.getGuestTeamId());
        water.setGuestTeamName(mainSports.getHomeTeamName());

        Map<String, Object> contrastInfo = new LinkedHashMap<>();
        contrastInfo.put("main_id", mainSports.getId());
        contrastInfo.put("home", home);
        contrastInfo.put("rp_id", rpSports.getId());
        contrastInfo.put("guest", guest);
        water.setContrastInfo(JSON.toJSONString(contrastInfo));

        water.setDisplay(display);
        water.setWater(Double.parseDouble(waterYield));
        water.setCreateTime(TimeUtils.format(new Date().getTime()));
        return water;
    }

    /**
     * 保存报水数据
     */
    private void saveWaterYield(List<WaterYield> waterYieldList) {
        if (!CollectionUtils.isEmpty(waterYieldList)) {
            waterYieldRepository.saveAll(waterYieldList);
            waterYieldRepository.flush();
        }
    }

    /**
     * 向微信推送
     */
    private void informWater(List<WaterYield> waterYieldList, Integer sendType) {
        if (!CollectionUtils.isEmpty(waterYieldList)) {
            int pageSize = 5;
            int total = waterYieldList.size();
            int pageCount = (total / pageSize) + ((total % pageSize > 0) ? 1 : 0);
            for (int i = 0; i < pageCount; i++) {
                int fromIndex = i * pageSize;
                int toIndex = (i == pageCount - 1 ? total : (i+1) * pageSize);
                List<WaterYield> subList = waterYieldList.subList(fromIndex, toIndex);
                for (WaterYield waterYield : subList) {
                    weiKongService.sendText(waterYield.getDisplay(), sendType);
                }

                try {
                    // 睡眠2秒,防止频率太快
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
