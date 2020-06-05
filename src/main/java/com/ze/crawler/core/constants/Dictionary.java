package com.ze.crawler.core.constants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 字典表
 */
public class Dictionary {
    /**
     * 电竞
     */
    // 电竞 - 联赛字典表   key: 赛事类型; value: (key: leagueName; value: leagueId)
    public static final Map<String, Map<String, String>> ESPORT_PB_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_RG_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_TF_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_IM_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_FY_LEAGUE_MAPPING = new LinkedHashMap<>();
    // 电竞 - 队伍字典表   key: 赛事类型; value: (key: teamName; value: teamId)
    public static final Map<String, Map<String, String>> ESPORT_PB_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_RG_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_TF_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_IM_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_FY_TEAM_MAPPING = new LinkedHashMap<>();
    // 电竞 - 盘口字典表   key: 赛事类型; value: (key: dishName; value: dishId)
    public static final Map<String, Map<String, String>> ESPORT_PB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_RG_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_TF_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_IM_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_IM_DISH_DISPLAY_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_FY_DISH_MAPPING = new LinkedHashMap<>();
    // 电竞 - 盘口类型对应  key: dishId; value: dishType
    public static final Map<String, String> ESPORT_DISH_TYPE_MAPPING = new LinkedHashMap<>();

    /**
     * 体育
     */
    // 体育 - 联赛字典表   key: leagueName; value: leagueId
    public static final Map<String, String> SPORT_PB_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_YB_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_SB_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_IM_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_BTI_LEAGUE_MAPPING = new LinkedHashMap<>();
    // 体育 - 队伍字典表   key: leagueId; value: (key: teamName; value: teamId)
    public static final Map<String, Map<String, String>> SPORT_PB_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_YB_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_SB_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_IM_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_BTI_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    // 体育 - 盘口字典表   key: dishName; value: dishId
    public static final Map<String, String> SPORT_SOCCER_PB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_BASKETBALL_PB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_SOCCER_YB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_BASKETBALL_YB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_SOCCER_SB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_BASKETBALL_SB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_SOCCER_IM_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_BASKETBALL_IM_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_SOCCER_BTI_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> SPORT_BASKETBALL_BTI_DISH_MAPPING = new LinkedHashMap<>();

    // 体育 - 盘口类型对应  key: dishId; value: dishType
    public static final Map<String, String> SPORT_DISH_TYPE_MAPPING = new LinkedHashMap<>();

    /**
     * 根据赛事类型和盘口类型获取对应映射 - 体育
     * @param type
     * @param dishType
     * @return
     */
    public static Map<String, String> getSportDishMappingByTypeAndDishType(String type, Integer dishType) {
        if (type.equalsIgnoreCase(Constant.SPORTS_TYPE_SOCCER)) {
            if (Constant.SPORTS_DISH_PB.equals(dishType)) {
                return SPORT_SOCCER_PB_DISH_MAPPING;
            } else if (Constant.SPORTS_DISH_IM.equals(dishType)) {
                return SPORT_SOCCER_IM_DISH_MAPPING;
            } else if (Constant.SPORTS_DISH_YB.equals(dishType)) {
                return SPORT_SOCCER_YB_DISH_MAPPING;
            } else if (Constant.SPORTS_DISH_SB.equals(dishType)) {
                return SPORT_SOCCER_SB_DISH_MAPPING;
            } else if (Constant.SPORTS_DISH_BTI.equals(dishType)) {
                return SPORT_SOCCER_BTI_DISH_MAPPING;
            }
        } else if (type.equalsIgnoreCase(Constant.SPORTS_TYPE_BASKETBALL)) {
            if (Constant.SPORTS_DISH_PB.equals(dishType)) {
                return SPORT_BASKETBALL_PB_DISH_MAPPING;
            } else if (Constant.SPORTS_DISH_IM.equals(dishType)) {
                return SPORT_BASKETBALL_IM_DISH_MAPPING;
            } else if (Constant.SPORTS_DISH_YB.equals(dishType)) {
                return SPORT_BASKETBALL_YB_DISH_MAPPING;
            } else if (Constant.SPORTS_DISH_SB.equals(dishType)) {
                return SPORT_BASKETBALL_SB_DISH_MAPPING;
            } else if (Constant.SPORTS_DISH_BTI.equals(dishType)) {
                return SPORT_BASKETBALL_BTI_DISH_MAPPING;
            }
        }

        return SPORT_SOCCER_PB_DISH_MAPPING;
    }

    /**
     * 根据赛事类型和盘口类型获取对应映射 - 电竞
     * @param type
     * @param dishType
     * @return
     */
    public static Map<String, String> getEsportDishMappingByTypeAndDishType(String type, Integer dishType) {
        if (Constant.ESPORTS_DISH_PB.equals(dishType)) {
            return ESPORT_PB_DISH_MAPPING.get(type);
        } else if (Constant.ESPORTS_DISH_RG.equals(dishType)) {
            return ESPORT_RG_DISH_MAPPING.get(type);
        } else if (Constant.ESPORTS_DISH_TF.equals(dishType)) {
            return ESPORT_TF_DISH_MAPPING.get(type);
        } else if (Constant.ESPORTS_DISH_IM.equals(dishType)) {
            return ESPORT_IM_DISH_MAPPING.get(type);
        } else if (Constant.ESPORTS_DISH_FY.equals(dishType)) {
            return ESPORT_FY_DISH_MAPPING.get(type);
        }

        return ESPORT_PB_DISH_MAPPING.get(type);
    }

    /**
     * 获取IM的盘口显示映射 - 电竞
     * @param type
     * @return
     */
    public static Map<String, String> getImDishDisplayMappingByType(String type) {
        return ESPORT_IM_DISH_DISPLAY_MAPPING.get(type);
    }
}
