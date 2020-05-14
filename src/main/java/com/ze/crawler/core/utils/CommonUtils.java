package com.ze.crawler.core.utils;

import java.math.BigDecimal;

/**
 * 工具类
 */
public class CommonUtils {

    /**
     * 计算水量
     * @param odds1
     * @param odds2
     * @return
     */
    public static String calculateWaterYield(String odds1, String odds2) {
        double d1 = parseDouble(odds1);
        double d2 = parseDouble(odds2);
        // 1. 假设odds1下注100, 计算odds2需要下注多少
        double amountBet = d1 * 100 / d2;
        // 2. 假设odds1赢
        Double water1 = d1 * 100 - (100 + amountBet);
        // 3. 假设odds2赢
        Double water2 = d2 * amountBet - ((100 + amountBet));
        // 4. 取平均值
        double waterYield = (water1 + water2) / 2;
        waterYield = setScale(waterYield, 2);

        return Double.toString(waterYield);
    }

    /**
     * 将字符串转换成Double
     * @param d
     * @return
     */
    public static Double parseDouble(String d) {
        return Double.valueOf(d);
    }

    /**
     * 保留精度
     * @param d
     * @param newScale
     * @return
     */
    public static double setScale(double d, int newScale) {
        BigDecimal bigDecimal = new BigDecimal(d);
        return bigDecimal.setScale(newScale, BigDecimal.ROUND_DOWN).doubleValue();
    }

    /**
     * 获取秒数
     * @param ms
     * @return
     */
    public static long getSeconds(long ms) {
        return ms / 1000;
    }
}
