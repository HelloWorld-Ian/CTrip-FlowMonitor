package com.ctrip.ibu.flow.monitor.service.constant;

/**
 * 时间转换为毫秒
 */
public enum TimeConvert {
    SECOND(1000),
    MINUTE(1000 * 60),
    HOUR(1000 * 60 * 60),
    DAY(1000 * 60 * 60 * 24),
    DEFAULT(1000 * 60);

    public long convert;
    TimeConvert(Integer convert) {
        this.convert = convert;
    }

    public static long convert(String timeUnit) {
        TimeConvert timeConvert;
        try {
            timeConvert = TimeConvert.valueOf(timeUnit);
            return timeConvert.convert;
        } catch (Exception e) {
            return TimeConvert.DEFAULT.convert;
        }
    }
}
