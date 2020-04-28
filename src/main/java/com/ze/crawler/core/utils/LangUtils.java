package com.ze.crawler.core.utils;

import java.util.UUID;

public class LangUtils {

    public static String generateUuid() {
        String uuid = UUID.randomUUID().toString();
        //将中划线去掉.
        uuid = uuid.replace("-", "");
        return uuid;
    }
}
