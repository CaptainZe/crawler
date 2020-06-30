package com.ze.crawler.core.service.display;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.entity.Sports;
import org.springframework.stereotype.Service;

/**
 * 报水显示
 */
@Service
public class DisplayService {

    /**
     * 报水显示
     * @param dishType      盘口类型
     * @param waterYield    水量
     * @param mainDish      主盘类型
     * @param mainSports    主盘记录
     * @param home          true表示mainSports使用home部分；false表示mainSports使用guest部分
     * @param rpDish        对手盘类型
     * @param rpSports      对手盘记录
     * @param guest         true表示rpSports使用guest部分；false表示rpSports使用home部分
     * @return
     */
    public String display(String dishType, String waterYield,
                                 Integer mainDish, Sports mainSports, boolean home,
                                 Integer rpDish, Sports rpSports, boolean guest) {
        String mainDishName = getDishName(mainDish);
        String rpDishName = getDishName(rpDish);

        StringBuilder sb = new StringBuilder();
        sb.append("类型：").append(mainSports.getType()).append("_").append(mainSports.getDishName());
        sb.append(Constant.NEW_LINE);
        sb.append(mainDishName).append("：").append(mainSports.getLeagueName());
        sb.append(Constant.NEW_LINE);
        sb.append(mainDishName).append("：").append(mainSports.getHomeTeamName()).append(Constant.VS).append(mainSports.getGuestTeamName());
        sb.append(Constant.NEW_LINE);
        sb.append(rpDishName).append("：").append(rpSports.getLeagueName());
        sb.append(Constant.NEW_LINE);
        sb.append(rpDishName).append("：").append(rpSports.getHomeTeamName()).append(Constant.VS).append(rpSports.getGuestTeamName());
        sb.append(Constant.NEW_LINE);
        sb.append(mainDishName).append("开赛时间：").append(mainSports.getStartTime());
        sb.append(Constant.NEW_LINE);
        sb.append(rpDishName).append("开赛时间：").append(rpSports.getStartTime());
        sb.append(Constant.NEW_LINE);

        // 关键显示
        // 盘口名可能存在通配符,进行替换显示
        String mainDishNameDisplay = mainSports.getDishName();
        if (mainDishNameDisplay.contains(Constant.DISH_NAME_WILDCARD_T1)) {
            mainDishNameDisplay = mainDishNameDisplay.replace(Constant.DISH_NAME_WILDCARD_T1, mainSports.getHomeTeamName());
        } else if (mainDishNameDisplay.contains(Constant.DISH_NAME_WILDCARD_T2)) {
            mainDishNameDisplay = mainDishNameDisplay.replace(Constant.DISH_NAME_WILDCARD_T2, mainSports.getGuestTeamName());
        }
        String rpDishNameDisplay = rpSports.getDishName();
        if (rpDishNameDisplay.contains(Constant.DISH_NAME_WILDCARD_T1)) {
            rpDishNameDisplay = rpDishNameDisplay.replace(Constant.DISH_NAME_WILDCARD_T1, rpSports.getHomeTeamName());
        } else if (rpDishNameDisplay.contains(Constant.DISH_NAME_WILDCARD_T2)) {
            rpDishNameDisplay = rpDishNameDisplay.replace(Constant.DISH_NAME_WILDCARD_T2, rpSports.getGuestTeamName());
        }

        if (Constant.DISH_TYPE_SYP.equals(dishType)) {
            String mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_[" + mainSports.getHomeTeamName() + "]_(" + mainSports.getHomeTeamOdds() + ")";
            if (!home) {
                mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_[" + mainSports.getGuestTeamName() + "]_(" + mainSports.getGuestTeamOdds() + ")";
            }
            sb.append(mainDishDisplay);
            sb.append(Constant.NEW_LINE);
            String rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_[" + rpSports.getGuestTeamName() + "]_(" + rpSports.getGuestTeamOdds() + ")";
            if (!guest) {
                rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_[" + rpSports.getHomeTeamName() + "]_(" + rpSports.getHomeTeamOdds() + ")";
            }
            sb.append(rpDishDisplay);
        } else if (Constant.DISH_TYPE_RFP.equals(dishType)) {
            String mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_[" + mainSports.getHomeTeamName() + "]_" + mainSports.getHomeTeamItem() + "_(" + mainSports.getHomeTeamOdds() + ")";
            if (!home) {
                mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_[" + mainSports.getGuestTeamName() + "]_" + mainSports.getGuestTeamItem() + "_(" + mainSports.getGuestTeamOdds() + ")";
            }
            sb.append(mainDishDisplay);
            sb.append(Constant.NEW_LINE);
            String rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_[" + rpSports.getGuestTeamName() + "]_" + rpSports.getGuestTeamItem() + "_(" + rpSports.getGuestTeamOdds() + ")";
            if (!guest) {
                rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_[" + rpSports.getHomeTeamName() + "]_" + rpSports.getHomeTeamItem() + "_(" + rpSports.getHomeTeamOdds() + ")";
            }
            sb.append(rpDishDisplay);
        } else if (Constant.DISH_TYPE_DXP.equals(dishType) || Constant.DISH_TYPE_DXP_IGNORE.equals(dishType)) {
            String mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_" + mainSports.getHomeExtraDishName() + "_" + mainSports.getHomeTeamItem() + "_(" + mainSports.getHomeTeamOdds() + ")";
            if (!home) {
                mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_" + mainSports.getGuestExtraDishName() + "_" + mainSports.getHomeTeamItem() + "_(" + mainSports.getGuestTeamOdds() + ")";
            }
            sb.append(mainDishDisplay);
            sb.append(Constant.NEW_LINE);
            String rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_" + rpSports.getGuestExtraDishName() + "_" + rpSports.getHomeTeamItem() + "_(" + rpSports.getGuestTeamOdds() + ")";
            if (!guest) {
                rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_" + rpSports.getHomeExtraDishName() + "_" + rpSports.getHomeTeamItem() + "_(" + rpSports.getHomeTeamOdds() + ")";
            }
            sb.append(rpDishDisplay);
        } else if (Constant.DISH_TYPE_DSP.equals(dishType) || Constant.DISH_TYPE_SFP.equals(dishType)) {
            String mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_" + mainSports.getHomeExtraDishName() + "_(" + mainSports.getHomeTeamOdds() + ")";
            if (!home) {
                mainDishDisplay = mainDishName + "：" + mainDishNameDisplay + "_" + mainSports.getGuestExtraDishName() + "_(" + mainSports.getGuestTeamOdds() + ")";
            }
            sb.append(mainDishDisplay);
            sb.append(Constant.NEW_LINE);
            String rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_" + rpSports.getGuestExtraDishName() + "_(" + rpSports.getGuestTeamOdds() + ")";
            if (!guest) {
                rpDishDisplay = rpDishName + "：" + rpDishNameDisplay + "_" + rpSports.getHomeExtraDishName() + "_(" + rpSports.getHomeTeamOdds() + ")";
            }
            sb.append(rpDishDisplay);
        }

        sb.append(Constant.NEW_LINE);
        sb.append("水量：").append(waterYield);
        return sb.toString();
    }

    /**
     * 获取盘口名
     * fixme 扩展点
     * @param dish
     * @return
     */
    private String getDishName(Integer dish) {
        // 电竞模块
        if (Constant.ESPORTS_DISH_PB.equals(dish)) {
            return "平博电竞";
        } else if (Constant.ESPORTS_DISH_RG.equals(dish)) {
            return "RG电竞";
        } else if (Constant.ESPORTS_DISH_TF.equals(dish)) {
            return "TF电竞";
        } else if (Constant.ESPORTS_DISH_IM.equals(dish)) {
            return "IM电竞";
        } else if (Constant.ESPORTS_DISH_FY.equals(dish)) {
            return "泛亚电竞";
        }

        // 体育模块
        if (Constant.SPORTS_DISH_PB.equals(dish)) {
            return "平博体育";
        } else if (Constant.SPORTS_DISH_IM.equals(dish)) {
            return "IM体育";
        } else if (Constant.SPORTS_DISH_YB.equals(dish)) {
            return "188体育";
        } else if (Constant.SPORTS_DISH_SB.equals(dish)) {
            return "沙巴体育";
        } else if (Constant.SPORTS_DISH_BTI.equals(dish)) {
            return "BTI体育";
        }

        return null;
    }
}
