package com.ze.crawler.core.constants;

/**
 * 188盘口常量
 */
public class YbbConstant {

    // 基础URL (%s替换替换顺序: ts)
    public final static String YBB_BASE_URL = "https://landing-sb.188sbk.com/zh-cn/Service/CentralService?GetData&ts=%s";

    // 盘口类型
    public final static String ODDS_TYPE_RFP = "ah";
    public final static String ODDS_TYPE_RFP_1ST = "ah1st";
    public final static String ODDS_TYPE_DXP = "ou";
    public final static String ODDS_TYPE_DXP_1ST = "ou1st";

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
}
