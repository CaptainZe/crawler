package com.ze.crawler.core.constants;

/**
 * RG盘口常量
 */
public class RGConstant {

    // 基础URL (%s替换替换顺序: page, match_type)
    public final static String RG_BASE_URL = "https://gamesinfo.esp01.com/v2/match?page=%s&match_type=%s";

    // 获取更多赔率 (%s替换替换顺序: match_id)
    public final static String RG_MORE_URL = "https://gamesinfo.esp01.com/v2/odds?match_id=%s";

    // match_type 盘类型
    public final static Integer MATCH_TYPE_TODAY = 2;   // 今日
    public final static Integer MATCH_TYPE_ZP = 3;      // 赛前

    /**
     * 一些常量
     */
    public static final Integer MAX_PAGE = 5;

    public static final Integer RESULT_ITEM_STATUS_NORMAL = 1;          // 比赛未开始
    public static final Integer RESULT_ITEM_ODDS_STATUS_NORMAL = 1;     // 盘口开放

    public static final String GAME_NAME_LOL = "英雄联盟";   // 70
    public static final String GAME_NAME_DOTA2 = "DOTA2";   // 151
    public static final String GAME_NAME_CSGO = "CSGO";     // 140
    public static final String GAME_NAME_KPL = "王者荣耀";     // 74

    public static final String MATCH_STAGE_FINAL = "final"; // 全场
    public static final String MATCH_STAGE_R1 = "r1"; // 第一局
    public static final String MATCH_STAGE_R2 = "r2"; // 第二局
    public static final String MATCH_STAGE_R3= "r3"; // 第三局
    public static final String MATCH_STAGE_MAP1= "map1"; // 地图一
    public static final String MATCH_STAGE_MAP2= "map2"; // 地图二
    public static final String MATCH_STAGE_MAP3= "map3"; // 地图三

    public static final String ROUND_BO3 = "bo3";

    // 盘口的一些描述信息
    public static final String EXTRA_DISH_NAME_GREATER_THAN = "大于";
    public static final String EXTRA_DISH_NAME_LESS_THAN = "小于";
    public static final String EXTRA_DISH_NAME_ODD = "单";
    public static final String EXTRA_DISH_NAME_EVEN = "双";
}
