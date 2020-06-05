package com.ze.crawler.core.constants;

/**
 * 全局常量
 */
public class Constant {
    // 请求头
    public final static String REQUEST_HEADER_USER_AGENT = "user-agent";
    public final static String REQUEST_HEADER_AUTHORIZATION = "Authorization";
    public final static String REQUEST_HEADER_CONTENT_TYPE_APPLICATION_JSON = "application/json";
    // 浏览器Agent
    public final static String CHROME_BROWSER_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36";

    // 重试次数
    public final static int RETRY_COUNT = 3;
    // 超时时间 （单位：毫秒）
    public final static int SOCKET_TIMEOUT = 30000;
    // 执行器超时时间 （单位：秒）
    public final static int EXECUTOR_TIMEOUT = 600;
    // 换行符
    public final static String NEW_LINE = System.getProperty("line.separator");

    // VS
    public final static String VS = " VS ";
    // 盘口名通配符
    public static final String DISH_NAME_WILDCARD_T1 = "$T1";
    public static final String DISH_NAME_WILDCARD_T2 = "$T2";

    // 映射解析路径
    public static final String ESPORT_MAPPING_FILE_PATH = "D:/Crawler/mapping/esport_mapping_new.xls";  // 电竞
    public static final String SPORT_MAPPING_FILE_PATH = "D:/Crawler/mapping/sport_mapping.xls";    // 体育

    // 日志类型
    // 电竞
    public final static int LOG_TYPE_PARSE_ESPORTS_ERROR = 1;                   // 电竞解析错误
    public final static int LOG_TYPE_ESPORTS_INVOKE_ERROR = 2;                  // 电竞爬虫调用错误
    public final static int LOG_TYPE_ESPORTS_WATER_CALCULATE_ERROR = 3;         // 电竞水量计算错误
    // 体育
    public final static int LOG_TYPE_PARSE_SPORTS_ERROR = 4;                    // 体育解析错误

    // 各下注盘类型
    public final static String DISH_TYPE_SYP = "输赢盘";
    public final static String DISH_TYPE_RFP = "让分盘";
    public final static String DISH_TYPE_DXP = "大小盘";
    public final static String DISH_TYPE_DSP = "单双盘";
    public final static String DISH_TYPE_SFP = "是否盘";
    public final static String DISH_TYPE_DXP_IGNORE = "大小盘(忽略大小)";

    // 电竞 - 类型
    public final static String ESPORTS_TYPE_LOL = "LOL";
    public final static String ESPORTS_TYPE_DOTA2 = "DOTA2";
    public final static String ESPORTS_TYPE_CSGO = "CSGO";
    public final static String ESPORTS_TYPE_KPL = "王者荣耀";
    // 电竞 - 盘口类型
    public final static Integer ESPORTS_DISH_PB = 11;
    public final static Integer ESPORTS_DISH_RG = 12;
    public final static Integer ESPORTS_DISH_TF = 13;
    public final static Integer ESPORTS_DISH_IM = 14;
    public final static Integer ESPORTS_DISH_FY = 15;

    // 体育 - 类型
    public final static String SPORTS_TYPE_SOCCER = "SOCCER";
    public final static String SPORTS_TYPE_BASKETBALL = "BASKETBALL";
    // 体育 - 盘口类型
    public final static Integer SPORTS_DISH_PB = 21;
    public final static Integer SPORTS_DISH_IM = 22;
    public final static Integer SPORTS_DISH_YB = 23;
    public final static Integer SPORTS_DISH_SB = 24;
    public final static Integer SPORTS_DISH_BTI = 25;
}
