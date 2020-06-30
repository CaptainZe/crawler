package com.ze.crawler.core.constants;

import java.util.HashMap;
import java.util.Map;

public class ProxyConstant {

    // IP代理
    public static String PROXY_HOST = "0";
    public static int PROXY_PORT = 0;

    // 是否使用代理的开关
    public static boolean USE_PROXY = false;
    public static Map<Integer, Boolean> DISH_USE_PROXY = new HashMap<>();

    // 代理IP生成场景
    public static String SCENE_ON_OPEN = "ON_OPEN";
    public static String SCENE_ON_SWITCH = "ON_SWITCH";
    public static String SCENE_ON_TASK = "ON_TASK";
}
