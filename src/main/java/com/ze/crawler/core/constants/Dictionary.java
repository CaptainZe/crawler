package com.ze.crawler.core.constants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 字典表
 */
public class Dictionary {

    // 电竞 - 联赛字典表   key: leagueName; value: leagueId
    public static final Map<String, String> ESPORT_PB_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_RG_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_TF_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_IM_LEAGUE_MAPPING = new LinkedHashMap<>();

    // 电竞 - 队伍字典表   key: leagueId; value: (key: teamName; value: teamId)
    public static final Map<String, Map<String, String>> ESPORT_PB_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_RG_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_TF_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_IM_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();

    // 电竞 - 盘口字典表   key: dishName; value: dishId
    public static final Map<String, String> ESPORT_LOL_PB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_LOL_RG_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_LOL_TF_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_LOL_IM_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_LOL_IM_DISH_DISPLAY_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_DOTA2_PB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_DOTA2_RG_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_DOTA2_TF_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_DOTA2_IM_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_DOTA2_IM_DISH_DISPLAY_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_CSGO_PB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_CSGO_RG_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_CSGO_TF_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_CSGO_IM_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, String> ESPORT_CSGO_IM_DISH_DISPLAY_MAPPING = new LinkedHashMap<>();

    // 电竞 - 盘口类型对应  key: dishId; value: dishType
    public static final Map<String, String> ESPORT_DISH_TYPE_MAPPING = new LinkedHashMap<>();

    /**
     * 根据赛事类型和盘口类型获取对应映射
     * @param type
     * @param dishType
     * @return
     */
    public static Map<String, String> getEsportDishMappingByTypeAndDishType(String type, Integer dishType) {
        if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_LOL)) {
            if (Constant.ESPORTS_DISH_PB.equals(dishType)) {
                return ESPORT_LOL_PB_DISH_MAPPING;
            } else if (Constant.ESPORTS_DISH_RG.equals(dishType)) {
                return ESPORT_LOL_RG_DISH_MAPPING;
            } else if (Constant.ESPORTS_DISH_TF.equals(dishType)) {
                return ESPORT_LOL_TF_DISH_MAPPING;
            } else if (Constant.ESPORTS_DISH_IM.equals(dishType)) {
                return ESPORT_LOL_IM_DISH_MAPPING;
            }
        } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_DOTA2)) {
            if (Constant.ESPORTS_DISH_PB.equals(dishType)) {
                return ESPORT_DOTA2_PB_DISH_MAPPING;
            } else if (Constant.ESPORTS_DISH_RG.equals(dishType)) {
                return ESPORT_DOTA2_RG_DISH_MAPPING;
            } else if (Constant.ESPORTS_DISH_TF.equals(dishType)) {
                return ESPORT_DOTA2_TF_DISH_MAPPING;
            } else if (Constant.ESPORTS_DISH_IM.equals(dishType)) {
                return ESPORT_DOTA2_IM_DISH_MAPPING;
            }
        } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_CSGO)) {
            if (Constant.ESPORTS_DISH_PB.equals(dishType)) {
                return ESPORT_CSGO_PB_DISH_MAPPING;
            } else if (Constant.ESPORTS_DISH_RG.equals(dishType)) {
                return ESPORT_CSGO_RG_DISH_MAPPING;
            } else if (Constant.ESPORTS_DISH_TF.equals(dishType)) {
                return ESPORT_CSGO_TF_DISH_MAPPING;
            } else if (Constant.ESPORTS_DISH_IM.equals(dishType)) {
                return ESPORT_CSGO_IM_DISH_MAPPING;
            }
        }

        return ESPORT_LOL_PB_DISH_MAPPING;
    }

    /**
     * 获取IM的盘口显示映射
     * @param type
     * @return
     */
    public static Map<String, String> getImDishDisplayMappingByType(String type) {
        if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_LOL)) {
            return ESPORT_LOL_IM_DISH_DISPLAY_MAPPING;
        } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_DOTA2)) {
            return ESPORT_DOTA2_IM_DISH_DISPLAY_MAPPING;
        } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_CSGO)) {
            return ESPORT_CSGO_IM_DISH_DISPLAY_MAPPING;
        }

        return ESPORT_LOL_IM_DISH_DISPLAY_MAPPING;
    }
}
