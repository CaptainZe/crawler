package com.ze.crawler.core.constants;

/**
 * 平博盘口常量
 */
public class PBConstant {

    // 基础URL (%s替换替换顺序: mk, sp, d, _)
    public final static String PB_BASE_URL =
            "https://k2dfy5.tender88.com/sports-service/sv/odds/events?" +
            "mk=%s&sp=%s&ot=1&btg=1&o=1&lg=&ev=&d=%s&l=3&v=0&me=0" +
            "&more=false&c=CN&tm=0&g=&pa=0&cl=3&_g=1&_=%s&locale=zh_CN";

    // 获取更多赔率 (%s替换替换顺序: mk, me, _)
    public final static String PB_MORE_URL = "https://k2dfy5.tender88.com/sports-service/sv/odds/events?" +
            "mk=%s&ot=1&btg=1&v=0&me=%s&more=true&c=CN&g=&pa=0&cl=3&pn=-1&_g=1&_=%s&locale=zh_CN";

    // MK 盘类型
    public final static String MK_ZP = "0";     // 早盘
    public final static String MK_TODAY = "1";  // 今天
    public final static String MK_MORE = "3";   // 更多

    // SP 赛事类型
    public final static String SP_BASKETBALL = "4";     // 篮球
    public final static String SP_SOCCER = "29";        // 足球
    public final static String SP_ESPORTS = "12";       // 电竞

    /**
     * 一些常量
     */
    public static final String TEAM_NAME_KILL_SUFFIX = "（击杀数）";
    public static final String LEAGUE_PREFIX_LOL = "英雄联盟";
    public static final String LEAGUE_PREFIX_LOL_EN = "League of Legends";
    public static final String LEAGUE_PREFIX_DOTA2 = "Dota 2";
    public static final String LEAGUE_PREFIX_CSGO = "CS:GO";

    // 盘口的一些描述信息
    public static final String EXTRA_DISH_NAME_DP = "大盘";
    public static final String EXTRA_DISH_NAME_XP = "小盘";

    // 电竞 - 自定义的盘口名 （这个几个字典表中的名字不可变）
    public static final String CUSTOM_DISH_NAME_WHOLE_SYP = "比赛_输赢盘";
    public static final String CUSTOM_DISH_NAME_WHOLE_RFP = "比赛_让分盘";
    public static final String CUSTOM_DISH_NAME_WHOLE_DXP = "比赛_大小盘";
    public static final String CUSTOM_DISH_NAME_MAP1_SYP = "地图1_输赢盘";
    public static final String CUSTOM_DISH_NAME_MAP2_SYP = "地图2_输赢盘";
    public static final String CUSTOM_DISH_NAME_MAP3_SYP = "地图3_输赢盘";
    // 击杀数或回合数
    public static final String CUSTOM_DISH_NAME_KILL_MAP1_RFP = "地图1_让分盘";
    public static final String CUSTOM_DISH_NAME_KILL_MAP1_DXP = "地图1_大小盘";
    public static final String CUSTOM_DISH_NAME_KILL_MAP2_RFP = "地图2_让分盘";
    public static final String CUSTOM_DISH_NAME_KILL_MAP2_DXP = "地图2_大小盘";
    public static final String CUSTOM_DISH_NAME_KILL_MAP3_RFP = "地图3_让分盘";
    public static final String CUSTOM_DISH_NAME_KILL_MAP3_DXP = "地图3_大小盘";
    public static final String CUSTOM_DISH_NAME_KILL_MAP1_HOME_TEAM_TOTAL = "地图1_球队总得分_主队进球";
    public static final String CUSTOM_DISH_NAME_KILL_MAP1_GUEST_TEAM_TOTAL = "地图1_球队总得分_客队进球";
    public static final String CUSTOM_DISH_NAME_KILL_MAP2_HOME_TEAM_TOTAL = "地图2_球队总得分_主队进球";
    public static final String CUSTOM_DISH_NAME_KILL_MAP2_GUEST_TEAM_TOTAL = "地图2_球队总得分_客队进球";
    public static final String CUSTOM_DISH_NAME_KILL_MAP3_HOME_TEAM_TOTAL = "地图3_球队总得分_主队进球";
    public static final String CUSTOM_DISH_NAME_KILL_MAP3_GUEST_TEAM_TOTAL = "地图3_球队总得分_客队进球";

    // 体育 - 自定义的盘口名 （这个几个字典表中的名字不可变）
    public static final String CUSTOM_DISH_NAME_FULL_SYP = "全场_独赢";
    public static final String CUSTOM_DISH_NAME_FULL_RFP = "全场_让分盘";
    public static final String CUSTOM_DISH_NAME_FULL_DXP = "全场_大小盘";
    public static final String CUSTOM_DISH_NAME_FULL_HOME_TEAM_TOTAL = "全场_球队总得分_主队进球";
    public static final String CUSTOM_DISH_NAME_FULL_GUEST_TEAM_TOTAL = "全场_球队总得分_客队进球";

    public static final String CUSTOM_DISH_NAME_FIRST_HALF_SYP = "上半场_独赢";
    public static final String CUSTOM_DISH_NAME_FIRST_HALF_RFP = "上半场_让分盘";
    public static final String CUSTOM_DISH_NAME_FIRST_HALF_DXP = "上半场_大小盘";
    public static final String CUSTOM_DISH_NAME_FIRST_HALF_HOME_TEAM_TOTAL = "上半场_球队总得分_主队进球";
    public static final String CUSTOM_DISH_NAME_FIRST_HALF_GUEST_TEAM_TOTAL = "上半场_球队总得分_客队进球";
}
