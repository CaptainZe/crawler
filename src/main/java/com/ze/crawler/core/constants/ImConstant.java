package com.ze.crawler.core.constants;

/**
 * IM盘口常量
 */
public class ImConstant {

    // 基础URL
    public final static String IM_BASE_URL = "https://imes-vkg.roshan88.com/Sportsbook/GetMatch";

    // 获取更多赔率 (%s替换替换顺序: ParentMatchNo)
    public final static String IM_MORE_URL = "https://imes-vkg.roshan88.com/Sportsbook/GetMatchDetail?ParentMatchNo=%s";

    // 赛事类型
    public final static Integer SPORT_ID_CSGO = 47;   // CS:GO
    public final static Integer SPORT_ID_DOTA2 = 46;  // DOTA 2
    public final static Integer SPORT_ID_LOL = 45;    // LOL

    /**
     * 一些常量
     */
    public final static Integer GAME_STATUS = 0; // 未开始

    public final static String SKIP_DISH = "PARENT"; // 跳过的盘口

    public final static String GAME_ROUND_BO5 = "BO5";
    public final static String GAME_ROUND_BO3 = "BO3";

    // 盘口的一些描述信息
    public static final String EXTRA_DISH_NAME_GREATER_THAN = "大于";
    public static final String EXTRA_DISH_NAME_LESS_THAN = "小于";
    public static final String EXTRA_DISH_NAME_ODD = "单";
    public static final String EXTRA_DISH_NAME_EVEN = "双";
}
