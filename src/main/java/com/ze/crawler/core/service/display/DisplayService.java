package com.ze.crawler.core.service.display;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.entity.Esports;
import org.springframework.stereotype.Service;

/**
 * 报水显示
 */
@Service
public class DisplayService {

    /**
     * 电竞 - 报水显示
     * @param dishType      盘口类型
     * @param waterYield    水量
     * @param mainDish      主盘类型
     * @param mainEsports   主盘记录
     * @param home          true表示mainEsports使用home部分；false表示mainEsports使用guest部分
     * @param rpDish        对手盘类型
     * @param rpEsports     对手盘记录
     * @param guest         true表示rpEsports使用guest部分；false表示rpEsports使用home部分
     * @return
     */
    public String displayESports(String dishType, String waterYield,
                                 Integer mainDish, Esports mainEsports, boolean home,
                                 Integer rpDish, Esports rpEsports, boolean guest) {
        String mainDishName = getEsportsDishName(mainDish);
        String rpDishName = getEsportsDishName(rpDish);

        StringBuilder sb = new StringBuilder();
        sb.append("类型：").append(mainEsports.getType()).append("_").append(mainEsports.getDishName());
        sb.append(Constant.NEW_LINE);
        sb.append(mainDishName).append("：").append(mainEsports.getLeagueName());
        sb.append(Constant.NEW_LINE);
        sb.append(mainDishName).append("：").append(mainEsports.getHomeTeamName()).append(Constant.VS).append(mainEsports.getGuestTeamName());
        sb.append(Constant.NEW_LINE);
        sb.append(rpDishName).append("：").append(rpEsports.getLeagueName());
        sb.append(Constant.NEW_LINE);
        sb.append(rpDishName).append("：").append(rpEsports.getHomeTeamName()).append(Constant.VS).append(rpEsports.getGuestTeamName());
        sb.append(Constant.NEW_LINE);
        sb.append(mainDishName).append("开赛时间：").append(mainEsports.getStartTime());
        sb.append(Constant.NEW_LINE);
        sb.append(rpDishName).append("开赛时间：").append(rpEsports.getStartTime());
        sb.append(Constant.NEW_LINE);

        // 关键显示
        // 盘口名可能存在通配符,进行替换显示
        String mainDishNameDisplay = mainEsports.getDishName();
        if (mainDishNameDisplay.contains(Constant.DISH_NAME_WILDCARD_T1)) {
            mainDishNameDisplay = mainDishNameDisplay.replace(Constant.DISH_NAME_WILDCARD_T1, mainEsports.getHomeTeamName());
        } else if (mainDishNameDisplay.contains(Constant.DISH_NAME_WILDCARD_T2)) {
            mainDishNameDisplay = mainDishNameDisplay.replace(Constant.DISH_NAME_WILDCARD_T2, mainEsports.getGuestTeamName());
        }
        String rpDishNameDisplay = rpEsports.getDishName();
        if (rpDishNameDisplay.contains(Constant.DISH_NAME_WILDCARD_T1)) {
            rpDishNameDisplay = rpDishNameDisplay.replace(Constant.DISH_NAME_WILDCARD_T1, rpEsports.getHomeTeamName());
        } else if (rpDishNameDisplay.contains(Constant.DISH_NAME_WILDCARD_T2)) {
            rpDishNameDisplay = rpDishNameDisplay.replace(Constant.DISH_NAME_WILDCARD_T2, rpEsports.getGuestTeamName());
        }

        if (Constant.DISH_TYPE_SYP.equals(dishType)) {
            String mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_[" + mainEsports.getHomeTeamName() + "]_(" + mainEsports.getHomeTeamOdds() + ")";
            if (!home) {
                mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_[" + mainEsports.getGuestTeamName() + "]_(" + mainEsports.getGuestTeamOdds() + ")";
            }
            sb.append(mainDishDisplay);
            sb.append(Constant.NEW_LINE);
            String rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_[" + rpEsports.getGuestTeamName() + "]_(" + rpEsports.getGuestTeamOdds() + ")";
            if (!guest) {
                rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_[" + rpEsports.getHomeTeamName() + "]_(" + rpEsports.getHomeTeamOdds() + ")";
            }
            sb.append(rpDishDisplay);
        } else if (Constant.DISH_TYPE_RFP.equals(dishType)) {
            String mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_[" + mainEsports.getHomeTeamName() + "]_" + mainEsports.getHomeTeamItem() + "_(" + mainEsports.getHomeTeamOdds() + ")";
            if (!home) {
                mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_[" + mainEsports.getGuestTeamName() + "]_" + mainEsports.getGuestTeamItem() + "_(" + mainEsports.getGuestTeamOdds() + ")";
            }
            sb.append(mainDishDisplay);
            sb.append(Constant.NEW_LINE);
            String rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_[" + rpEsports.getGuestTeamName() + "]_" + rpEsports.getGuestTeamItem() + "_(" + rpEsports.getGuestTeamOdds() + ")";
            if (!guest) {
                rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_[" + rpEsports.getHomeTeamName() + "]_" + rpEsports.getHomeTeamItem() + "_(" + rpEsports.getHomeTeamOdds() + ")";
            }
            sb.append(rpDishDisplay);
        } else if (Constant.DISH_TYPE_DXP.equals(dishType) || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
            String mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_" + mainEsports.getHomeExtraDishName() + "_" + mainEsports.getHomeTeamItem() + "_(" + mainEsports.getHomeTeamOdds() + ")";
            if (!home) {
                mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_" + mainEsports.getGuestExtraDishName() + "_" + mainEsports.getHomeTeamItem() + "_(" + mainEsports.getGuestTeamOdds() + ")";
            }
            sb.append(mainDishDisplay);
            sb.append(Constant.NEW_LINE);
            String rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_" + rpEsports.getGuestExtraDishName() + "_" + rpEsports.getHomeTeamItem() + "_(" + rpEsports.getGuestTeamOdds() + ")";
            if (!guest) {
                rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_" + rpEsports.getHomeExtraDishName() + "_" + rpEsports.getHomeTeamItem() + "_(" + rpEsports.getHomeTeamOdds() + ")";
            }
            sb.append(rpDishDisplay);
        } else if (Constant.DISH_TYPE_DSP.equals(dishType) || Constant.DISH_TYPE_SFP.equals(dishType)) {
            String mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_" + mainEsports.getHomeExtraDishName() + "_(" + mainEsports.getHomeTeamOdds() + ")";
            if (!home) {
                mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_" + mainEsports.getGuestExtraDishName() + "_(" + mainEsports.getGuestTeamOdds() + ")";
            }
            sb.append(mainDishDisplay);
            sb.append(Constant.NEW_LINE);
            String rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_" + rpEsports.getGuestExtraDishName() + "_(" + rpEsports.getGuestTeamOdds() + ")";
            if (!guest) {
                rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_" + rpEsports.getHomeExtraDishName() + "_(" + rpEsports.getHomeTeamOdds() + ")";
            }
            sb.append(rpDishDisplay);
        }

        sb.append(Constant.NEW_LINE);
        sb.append("水量：").append(waterYield);
        return sb.toString();
    }

    /**
     * 获取盘口名
     * @param dish
     * @return
     */
    private String getEsportsDishName(Integer dish) {
        if (Constant.ESPORTS_DISH_PB.equals(dish)) {
            return "平博电竞";
        } else if (Constant.ESPORTS_DISH_RG.equals(dish)) {
            return "RG电竞";
        } else if (Constant.ESPORTS_DISH_TF.equals(dish)) {
            return "TF电竞";
        } else if (Constant.ESPORTS_DISH_IM.equals(dish)) {
            return "IM电竞";
        }
        return null;
    }
}
