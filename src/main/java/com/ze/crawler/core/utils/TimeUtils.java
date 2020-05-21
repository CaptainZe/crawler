package com.ze.crawler.core.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 时间工具类
 */
public class TimeUtils {

    public final static String TIME_FORMAT_1 = "yyyy-MM-dd";
    public final static String TIME_FORMAT_2 = "yyyy-MM-dd HH:mm:ss";
    public final static String TIME_FORMAT_3 = "yyyy/MM/dd";

    /**
     * 获取日期 - 如果超过12点获取第二天日期
     * @return
     */
    public static String getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 12) {
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        }

        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_1);
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取第二天的23:59:59
     * @return
     */
    public static String getNextDayLastTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        // 2020-04-06 00:00:00
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_2);
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取第二天日期
     * @return
     */
    public static String getNextDay() {
        return getNextDay(TIME_FORMAT_1);
    }

    /**
     * 获取第二天日期
     * @return
     */
    public static String getNextDay(String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(calendar.getTime());
    }

    /**
     * 格式化时间戳
     * @param timestamp
     * @return
     */
    public static String format(Long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_2);
        return sdf.format(date);
    }
}
