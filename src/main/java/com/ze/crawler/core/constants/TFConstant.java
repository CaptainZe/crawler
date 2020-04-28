package com.ze.crawler.core.constants;

/**
 * TF盘口常量
 */
public class TFConstant {

    // 今日URL (%s替换替换顺序: game_id)
    public final static String TF_TODAY_URL = "https://api-v4.zly889.com/api/v4/events?game_id=%s&timing=today&market_option=match&lang=zh&combo=false";

    // 早盘URL (%s替换替换顺序: game_id, date)
    public final static String TF_ZP_URL = "https://api-v4.zly889.com/api/v4/events?game_id=%s&date=%s&market_option=match&lang=zh&combo=false";

    // 总局赔率URL (%s替换替换顺序: event_id)
    public final static String TF_MATCH_URL = "https://api-v4.zly889.com/api/v4/events?event_id=%s&market_option=MATCH&lang=zh&combo=false";

    // 各局赔率URL (%s替换替换顺序: event_id, map_option)
    public final static String TF_MAP_URL = "https://api-v4.zly889.com/api/v4/events?event_id=%s&market_option=map&map_option=%s&lang=zh&combo=false";

    // 赛事类型
    public final static Integer GAME_ID_CSGO = 1;   // CS:GO
    public final static Integer GAME_ID_DOTA2 = 2;  // DOTA 2
    public final static Integer GAME_ID_LOL = 3;    // LOL

    /**
     * 一些常量
     */
    // 总局 在今日或早盘中已经带有总局的赔率信息,没有必要再请求一次,可以过滤掉
    public final static String TAB_NAME_MATCH = "MATCH";
    public final static String TAB_NAME_MAP3 = "MAP 3";
    public final static String TAB_NAME_MAP4 = "MAP 4";
    public final static String TAB_NAME_MAP5 = "MAP 5";

    public static final String ROUND_BO3 = "BO3";
    public static final String ROUND_BO5 = "BO5";

    // 表示正常, 非滚球
    public final static String IN_PLAY_FALSE = "false";

    // 盘口是否开放
    public final static String MARKET_SELECTION_STATUS_OPEN = "open";

    // 主客队
    public final static String TEAM_HOME = "home";
    public final static String TEAM_GUEST = "away";
    // 大小盘
    public final static String DXP_OVER = "over";   // 大
    public final static String DXP_UNDER = "under"; // 小
    // 单双盘
    public final static String DSP_ODD = "odd";   // 单
    public final static String DSP_EVEN = "even"; // 双
    // 是否盘
    public final static String SFP_NO = "no";   // 不是
    public final static String SFP_YES = "yes"; // 是
}
