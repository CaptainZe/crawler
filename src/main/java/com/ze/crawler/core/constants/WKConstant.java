package com.ze.crawler.core.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 微控常量
 */
public class WKConstant {
    // 微控域名
    public final static String DOMAIN = "http://xingshenwk.com/";
    // Authorization 登录一次永不失效
    public final static String AUTHORIZATION = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwYXNzd29yZCI6ImUxMGFkYzM5NDliYTU5YWJiZTU2ZTA1N2YyMGY4ODNld1BDUDZvSVgyI0goMjVhcyIsImlzcyI6InhpbmdzaGVuZyIsImFjY291bnQiOiIxNTk1OTA0Njg5NCJ9.WA93YzyXsAcIjS0tvwPk0a886L_D20OpyLAWCqgo3x8";
    // 重试文本
    public final static String TRY_AGAIN = "请再试一次！";

    // 微控账号信息   Key: wId  Value:target
    public final static Map<String, String> WK_CHECK = new HashMap<>();          // 检查
    public final static Map<String, String> WK_ESPORTS = new HashMap<>();        // 电竞
    public final static Map<String, String> WK_ESPORTS_BP = new HashMap<>();     // 电竞 - 包赔
    public final static Map<String, String> WK_ESPORTS_ZD = new HashMap<>();     // 电竞 - 指定
    public final static Map<String, String> WK_SPORTS = new HashMap<>();         // 体育
    public final static Map<String, String> WK_SPORTS_BP = new HashMap<>();      // 体育 - 包赔
    public final static Map<Integer, Map<String, String>> WK_USAGE_INFO = new HashMap<>();

    // 发送类型
    public final static Integer SEND_TYPE_ESPORTS = 1;
    public final static Integer SEND_TYPE_ESPORTS_BP = 2;
    public final static Integer SEND_TYPE_SPORTS = 3;
    public final static Integer SEND_TYPE_SPORTS_BP = 4;
    public final static Integer SEND_TYPE_ESPORTS_ZD = 5;
    // 使用场景
    public final static Integer USAGE_ESPORT = 1;
    public final static Integer USAGE_SPORT = 2;

    public final static String ENABLE_TRUE = "true";
}
