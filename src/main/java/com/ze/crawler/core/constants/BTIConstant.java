package com.ze.crawler.core.constants;

/**
 * BTI盘口常量
 */
public class BTIConstant {

    // 今日URL(%s替换替换顺序: branchID)
    public static final String BTI_TODAY_URL = "https://rks.btisports.io/pagemethods_ros.aspx/GetAsianSkinTodayMasterEvents?todayOffset=6&branchID=%s&leagueID=-1&timeZone=8";
    // 早盘URL(%s替换替换顺序: branchID)
    public static final String BTI_EARLY_URL = "https://rks.btisports.io/pagemethods_ros.aspx/GetAsianSkinEarlyMarketMasterEvents?todayOffset=6&branchID=%s&leagueID=-1&day=0&timeZone=8";
    // 赔率URL
    public static final String BTI_ODDS_URL = "https://rks.btisports.io/pagemethods.aspx/UpdateAsianSkinEvents";

    // 类型
    public static final Integer BRANCH_ID_SOCCER = 1;
    public static final Integer BRANCH_ID_BASKETBALL = 2;

    // 需要的盘口类型
    public static final Integer ODDS_TYPE_0 = 0;

    // 体育 - 自定义的盘口名 （这个几个字典表中的名字不可变）
    public static final String CUSTOM_DISH_NAME_FULL_SYP = "全场_独赢";
    public static final String CUSTOM_DISH_NAME_FULL_RFP = "全场_让分盘";
    public static final String CUSTOM_DISH_NAME_FULL_DXP = "全场_大小盘";

    public static final String CUSTOM_DISH_NAME_FIRST_HALF_SYP = "上半场_独赢";
    public static final String CUSTOM_DISH_NAME_FIRST_HALF_RFP = "上半场_让分盘";
    public static final String CUSTOM_DISH_NAME_FIRST_HALF_DXP = "上半场_大小盘";

    // 盘口的一些描述信息
    public static final String EXTRA_DISH_NAME_GREATER_THAN = "大于";
    public static final String EXTRA_DISH_NAME_LESS_THAN = "小于";

    public static final String FULL_NAME = "全场";
    public static final String LEAGUE_NAME_IGNORE_FIFA = "FIFA";
}
