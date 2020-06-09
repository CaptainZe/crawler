package com.ze.crawler.core.constants;

/**
 * IM盘口常量
 */
public class IMConstant {

    // 基础URL
    public final static String IM_BASE_URL = "https://imes-vkg.roshan88.com/Sportsbook/GetMatch";
    // 获取更多赔率 (%s替换替换顺序: ParentMatchNo)
    public final static String IM_MORE_URL = "https://imes-vkg.roshan88.com/Sportsbook/GetMatchDetail?ParentMatchNo=%s";

    // [new]基础URL
    public final static String IM_BASE_URL_V1 = "https://imes-vkg.roshan88.com/api/GetIndexMatch";
    public final static String IM_MORE_URL_V1 = "https://imes-vkg.roshan88.com/api/GetMatchDetailsByParent";

    // 赛事类型
    public final static Integer SPORT_ID_CSGO = 47;   // CS:GO
    public final static Integer SPORT_ID_DOTA2 = 46;  // DOTA 2
    public final static Integer SPORT_ID_LOL = 45;    // LOL
    public final static Integer SPORT_ID_KPL = 48;    // 王者荣耀

    /**
     * 一些常量
     */
    public final static Integer GAME_STATUS_V1 = -1; // 未开始
    public final static Integer DISH_STATUS_V1 = 1; // 未锁
    public final static Integer HOME_TEAM_ODDS_CODE_V1 = 1;
    public final static Integer GUEST_TEAM_ODDS_CODE_V1 = 2;

    public final static Integer GAME_STATUS = 0; // 未开始
    public final static String SKIP_DISH = "PARENT"; // 跳过的盘口

    public final static String GAME_ROUND_BO7 = "BO7";
    public final static String GAME_ROUND_BO5 = "BO5";
    public final static String GAME_ROUND_BO3 = "BO3";

    // 盘口的一些描述信息
    public static final String EXTRA_DISH_NAME_GREATER_THAN = "大于";
    public static final String EXTRA_DISH_NAME_LESS_THAN = "小于";
    public static final String EXTRA_DISH_NAME_ODD = "单";
    public static final String EXTRA_DISH_NAME_EVEN = "双";

    // 体育URL
    public final static String IM_SPORT_BASE_URL = "https://sb.imspanaw.com/api/Event/getSportEvents";

    public final static Integer SPORT_ID_SOCCER = 1;      // 足球
    public final static Integer SPORT_ID_BASKETBALL = 2;  // 篮球

    public final static Integer MARKET_TODAY = 2;    // 今日
    public final static Integer MARKET_ZP = 1;       // 早盘

    public final static Integer BTI_RFP = 1;    // 让分盘
    public final static Integer BTI_DXP = 2;    // 大小盘
    public final static Integer BTI_SYP = 3;    // 输赢盘

    public final static Integer PI_FULL = 1;        // 全场
    public final static Integer PI_FIRST_HALF = 2;  // 上半场

    // 体育 - 自定义的盘口名 （这个几个字典表中的名字不可变）
    public static final String CUSTOM_DISH_NAME_FULL_SYP = "全场_独赢";
    public static final String CUSTOM_DISH_NAME_FULL_RFP = "全场_让分盘";
    public static final String CUSTOM_DISH_NAME_FULL_DXP = "全场_大小盘";

    public static final String CUSTOM_DISH_NAME_FIRST_HALF_SYP = "上半场_独赢";
    public static final String CUSTOM_DISH_NAME_FIRST_HALF_RFP = "上半场_让分盘";
    public static final String CUSTOM_DISH_NAME_FIRST_HALF_DXP = "上半场_大小盘";
}
