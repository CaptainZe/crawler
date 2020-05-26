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

    // Round
    public static final String ROUND_BO3 = "BO3";
    public static final String ROUND_BO5 = "BO5";
    public static final String ROUND_BO7 = "BO7";
}
