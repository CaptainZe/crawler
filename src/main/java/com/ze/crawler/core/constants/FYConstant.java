package com.ze.crawler.core.constants;

/**
 * 泛亚盘口常量
 */
public class FYConstant {

    // 基础URL
    public final static String FY_BASE_URL = "https://api.aabv.top/";

    // path
    public final static String PATH_MATCH_LIST = "/request/esport/match/list";
    public final static String PATH_MATCH_INFO = "/request/esport/Match/Info";

    // 比赛名
    public static final String GAME_NAME_LOL = "英雄联盟";       // 2
    public static final String GAME_NAME_DOTA2 = "刀塔II";      // 10
    public static final String GAME_NAME_CSGO = "反恐精英";     // 2025
    public static final String GAME_NAME_KPL = "王者荣耀";      // 2040

    // 状态
    public static final String MATCH_STATUS_BEGIN = "Begin";

    // 赔率index
    public static final int INDEX_FIRST = 0;
    public static final int INDEX_SECOND = 1;

    // Round
    public static final String ROUND_BO3 = "BO3";
    public static final String ROUND_BO5 = "BO5";
    public static final String ROUND_BO7 = "BO7";

    // 地图数
    public static final String ROUND_MAP3 = "Map3";
    public static final String ROUND_MAP4 = "Map4";
    public static final String ROUND_MAP5 = "Map5";

    // 盘口的一些描述信息
    public static final String EXTRA_DISH_NAME_GREATER_THAN = "大";
    public static final String EXTRA_DISH_NAME_LESS_THAN = "小";
    public static final String EXTRA_DISH_NAME_ODD = "单";
    public static final String EXTRA_DISH_NAME_EVEN = "双";
    public static final String EXTRA_DISH_NAME_YES = "是";
    public static final String EXTRA_DISH_NAME_NO = "否";
}
