package com.ze.crawler.core.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 微控常量
 */
public class WKConstant {
    // 微控域名
    public final static String DOMAIN = "http://xingshenapi.com/";
    // Authorization 登录一次永不失效
    public final static String AUTHORIZATION = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwYXNzd29yZCI6ImUxMGFkYzM5NDliYTU5YWJiZTU2ZTA1N2YyMGY4ODNld1BDUDZvSVgyI0goMjVhcyIsImlzcyI6InhpbmdzaGVuZyIsImFjY291bnQiOiIxNTk1OTA0Njg5NCJ9.WA93YzyXsAcIjS0tvwPk0a886L_D20OpyLAWCqgo3x8";
    // 重试文本
    public final static String TRY_AGAIN = "请再试一次！";

    // 微控账号信息   Key: wId  Value:target
    public final static Map<String, String> WK_ESPORTS_YL = new HashMap<>();    // 电竞 - 娱乐
    public final static Map<String, String> WK_ESPORTS_BP = new HashMap<>();    // 电竞 - 包赔

    // 发送类型
    public final static Integer ESPORTS_YL = 1;
    public final static Integer ESPORTS_BP = 2;

    public final static String ENABLE_TRUE = "true";
}
