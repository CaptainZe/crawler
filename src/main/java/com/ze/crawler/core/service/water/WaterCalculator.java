package com.ze.crawler.core.service.water;

import com.alibaba.fastjson.JSON;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.entity.Esports;
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
     * 计算电竞水量
     */
    public void calculateEsportsWater(Map<Integer, List<? extends Esports>> esportsMap, double threshold, Integer main) {
        Set<Integer> alreadyDoMain = new HashSet<>();

        if (main == null) {
            // 两两比较
            for (Integer mainKey : esportsMap.keySet()) {
                alreadyDoMain.add(mainKey);

                List<Esports> mianEsportsList = (List<Esports>) esportsMap.get(mainKey);
                if (!CollectionUtils.isEmpty(mianEsportsList)) {
                    for (Integer rp : esportsMap.keySet()) {
                        if (alreadyDoMain.contains(rp)) {
                            continue;
                        }

                        List<Esports> rpEsportsList = (List<Esports>) esportsMap.get(rp);
                        if (!CollectionUtils.isEmpty(rpEsportsList)) {
                            List<WaterYield> waterYieldList = new ArrayList<>();
                            for (Esports mainEsports : mianEsportsList) {
                                for (Esports rpEsports : rpEsportsList) {
                                    // 匹配对手盘
                                    List<WaterYield> waterYields = matchRivalPlateAndCalculateAndDisplay(mainKey, mainEsports, rp, rpEsports, threshold);
                                    if (!CollectionUtils.isEmpty(waterYields)) {
                                        waterYieldList.addAll(waterYields);
                                    }
                                }
                            }
                            // 保存
                            saveWaterYield(waterYieldList);
                        }
                    }
                }
            }
        } else {
            // 指定主盘口
            List<Esports> mianEsportsList = (List<Esports>) esportsMap.get(main);
            if (!CollectionUtils.isEmpty(mianEsportsList)) {
                for (Integer rp : esportsMap.keySet()) {
                    if (rp.equals(main)) {
                        continue;
                    }

                    List<Esports> rpEsportsList = (List<Esports>) esportsMap.get(rp);
                    if (!CollectionUtils.isEmpty(rpEsportsList)) {
                        List<WaterYield> waterYieldList = new ArrayList<>();
                        for (Esports mainEsports : mianEsportsList) {
                            for (Esports rpEsports : rpEsportsList) {
                                // 匹配对手盘
                                List<WaterYield> waterYields = matchRivalPlateAndCalculateAndDisplay(main, mainEsports, rp, rpEsports, threshold);
                                if (!CollectionUtils.isEmpty(waterYields)) {
                                    waterYieldList.addAll(waterYields);
                                }
                            }
                        }
                        // 保存
                        saveWaterYield(waterYieldList);
                    }
                }
            }
        }
    }

    /**
     * 匹配对手盘 & 计算水量 & 报水存储和推送
     */
    private List<WaterYield> matchRivalPlateAndCalculateAndDisplay(Integer mainDish, Esports mainEsports, Integer rpDish, Esports rpEsports, double threshold) {
        List<WaterYield> waterYields = new ArrayList<>();

        // 基础判断
        if (rpEsports.getType().equals(mainEsports.getType())
                && rpEsports.getDishId().equals(mainEsports.getDishId())
                && rpEsports.getLeagueId().equals(mainEsports.getLeagueId())) {

            // 盘口类型
            String dishType = Dictionary.ESPORT_DISH_TYPE_MAPPING.get(mainEsports.getDishId());

            // 由于各个盘口的主客队定义不一样，只要两队一样，就认为是同一场比赛
            if (rpEsports.getHomeTeamId().equals(mainEsports.getHomeTeamId())
                    && rpEsports.getGuestTeamId().equals(mainEsports.getGuestTeamId())) {
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
                    if (mainEsports.getHomeTeamItem().equals(rpEsports.getHomeTeamItem())) {
                        match = true;
                    }
                }

                if (match) {
                    // 都要计算两次
                    String waterYield1 = CommonUtils.calculateWaterYield(mainEsports.getHomeTeamOdds(), rpEsports.getGuestTeamOdds());
                    if (controlWaterYield(waterYield1, threshold)) {
                        String display = displayService.displayESports(dishType, waterYield1, mainDish, mainEsports, true, rpDish, rpEsports, true);
                        waterYields.add(getWaterYield(waterYield1, mainDish, mainEsports, true, rpDish, rpEsports, true, display));
                    }
                    String waterYield2 = CommonUtils.calculateWaterYield(mainEsports.getGuestTeamOdds(), rpEsports.getHomeTeamOdds());
                    if (controlWaterYield(waterYield2, threshold)) {
                        String display = displayService.displayESports(dishType, waterYield2, mainDish, mainEsports, false, rpDish, rpEsports, false);
                        waterYields.add(getWaterYield(waterYield2, mainDish, mainEsports, false, rpDish, rpEsports, false, display));
                    }
                }
            } else if (rpEsports.getGuestTeamId().equals(mainEsports.getHomeTeamId())
                        && rpEsports.getHomeTeamId().equals(mainEsports.getGuestTeamId())) {
                // 对手盘的主客队与主盘口不一致
                boolean match = false;
                if (Constant.DISH_TYPE_SYP.equals(dishType)
                        || Constant.DISH_TYPE_DSP.equals(dishType)
                        || Constant.DISH_TYPE_SFP.equals(dishType)) {
                    match = true;
                } else if (Constant.DISH_TYPE_RFP.equals(dishType)) {
                    // 让分盘需要判断让分是否一致；大小盘需要判断大小数是否一致
                    if (mainEsports.getHomeTeamItem().equals(rpEsports.getGuestTeamItem())) {
                        match = true;
                    }
                } else if (Constant.DISH_TYPE_DXP.equals(dishType)
                            || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
                    if (mainEsports.getHomeTeamItem().equals(rpEsports.getHomeTeamItem())) {
                        match = true;
                    }
                }

                if (match) {
                    // 都要计算两次
                    if (Constant.DISH_TYPE_DSP.equals(dishType)
                        || Constant.DISH_TYPE_SFP.equals(dishType)
                        || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)
                        || Constant.DISH_TYPE_DXP.equals(dishType)) {
                        // 都要计算两次
                        String waterYield1 = CommonUtils.calculateWaterYield(mainEsports.getHomeTeamOdds(), rpEsports.getGuestTeamOdds());
                        if (controlWaterYield(waterYield1, threshold)) {
                            String display = displayService.displayESports(dishType, waterYield1, mainDish, mainEsports, true, rpDish, rpEsports, true);
                            waterYields.add(getWaterYield(waterYield1, mainDish, mainEsports, true, rpDish, rpEsports, true, display));
                        }
                        String waterYield2 = CommonUtils.calculateWaterYield(mainEsports.getGuestTeamOdds(), rpEsports.getHomeTeamOdds());
                        if (controlWaterYield(waterYield2, threshold)) {
                            String display = displayService.displayESports(dishType, waterYield2, mainDish, mainEsports, false, rpDish, rpEsports, false);
                            waterYields.add(getWaterYield(waterYield2, mainDish, mainEsports, false, rpDish, rpEsports, false, display));
                        }
                    } else {
                        // 需要反转
                        String waterYield1 = CommonUtils.calculateWaterYield(mainEsports.getHomeTeamOdds(), rpEsports.getHomeTeamOdds());
                        if (controlWaterYield(waterYield1, threshold)) {
                            String display = displayService.displayESports(dishType, waterYield1, mainDish, mainEsports, true, rpDish, rpEsports, false);
                            waterYields.add(getWaterYield(waterYield1, mainDish, mainEsports, true, rpDish, rpEsports, false, display));
                        }
                        String waterYield2 = CommonUtils.calculateWaterYield(mainEsports.getGuestTeamOdds(), rpEsports.getGuestTeamOdds());
                        if (controlWaterYield(waterYield2, threshold)) {
                            String display = displayService.displayESports(dishType, waterYield2, mainDish, mainEsports, false, rpDish, rpEsports, true);
                            waterYields.add(getWaterYield(waterYield2, mainDish, mainEsports, false, rpDish, rpEsports, true, display));
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
    private WaterYield getWaterYield(String waterYield, Integer mainDish, Esports mainEsports, boolean home,
                                     Integer rpDish, Esports rpEsports, boolean guest, String display) {
        WaterYield water = new WaterYield();
        water.setId(LangUtils.generateUuid());
        water.setTaskId(mainEsports.getTaskId());
        water.setType(mainEsports.getType());
        water.setMainDish(mainDish);
        water.setRpDish(rpDish);
        water.setDishId(mainEsports.getDishId());
        water.setDishName(mainEsports.getDishName());
        water.setLeagueId(mainEsports.getLeagueId());
        water.setLeagueName(mainEsports.getLeagueName());
        water.setHomeTeamId(mainEsports.getHomeTeamId());
        water.setHomeTeamName(mainEsports.getHomeTeamName());
        water.setGuestTeamId(mainEsports.getGuestTeamId());
        water.setGuestTeamName(mainEsports.getHomeTeamName());

        Map<String, Object> contrastInfo = new LinkedHashMap<>();
        contrastInfo.put("main_id", mainEsports.getId());
        contrastInfo.put("home", home);
        contrastInfo.put("rp_id", rpEsports.getId());
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

            // fixme 后期不用微信推送
            informWater(waterYieldList);
        }
    }

    /**
     * 向微信推送
     */
    private void informWater(List<WaterYield> waterYieldList) {
        int pageSize = 5;
        int total = waterYieldList.size();
        int pageCount = (total / pageSize) + ((total % pageSize > 0) ? 1 : 0);
        for (int i = 0; i < pageCount; i++) {
            int fromIndex = i * pageSize;
            int toIndex = (i == pageCount - 1 ? total : (i+1) * pageSize);
            List<WaterYield> subList = waterYieldList.subList(fromIndex, toIndex);
            for (WaterYield waterYield : subList) {
                weiKongService.sendText(waterYield.getDisplay());
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
