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
    // 电竞 - 联赛字典表   key: type; value: (key: leagueName; value: leagueId)
    public static final Map<String, Map<String, String>> ESPORT_PB_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_RG_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_TF_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_IM_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_FY_LEAGUE_MAPPING = new LinkedHashMap<>();
    // 电竞 - 队伍字典表   key: leagueId; value: (key: teamName; value: teamId)
    public static final Map<String, Map<String, String>> ESPORT_PB_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_RG_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_TF_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_IM_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_FY_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    // 电竞 - 盘口字典表   key: type; value: (key: dishName; value: dishId)
    public static final Map<String, Map<String, String>> ESPORT_PB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_RG_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_TF_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_IM_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_FY_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> ESPORT_IM_DISH_DISPLAY_MAPPING = new LinkedHashMap<>();

    // 电竞 - 盘口类型对应  key: dishId; value: dishType
    public static final Map<String, String> ESPORT_DISH_TYPE_MAPPING = new LinkedHashMap<>();

    /**
     * 体育
     */
    // 体育 - 联赛字典表   key: type; value: (key: leagueName; value: leagueId)
    public static final Map<String, Map<String, String>> SPORT_PB_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_YB_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_SB_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_IM_LEAGUE_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_BTI_LEAGUE_MAPPING = new LinkedHashMap<>();
    // 体育 - 队伍字典表   key: leagueId; value: (key: teamName; value: teamId)
    public static final Map<String, Map<String, String>> SPORT_PB_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_YB_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_SB_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_IM_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_BTI_LEAGUE_TEAM_MAPPING = new LinkedHashMap<>();
    // 体育 - 盘口字典表   key: type; value: (key: dishName; value: dishId)
    public static final Map<String, Map<String, String>> SPORT_PB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_YB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_SB_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_IM_DISH_MAPPING = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> SPORT_BTI_DISH_MAPPING = new LinkedHashMap<>();

    // 体育 - 盘口类型对应  key: dishId; value: dishType
    public static final Map<String, String> SPORT_DISH_TYPE_MAPPING = new LinkedHashMap<>();
}
