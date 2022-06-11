package com.ctrip.ibu.flow.monitor.service.constant;

import java.util.Objects;

/**
 * @author Ian
 * @date 2022/6/3
 */
public enum DefaultSplits {
    SECOND(6),
    MINUTE(6),
    HOUR(6),
    DAY(6),
    DEFAULT(6);

    int splits;

    public static int get(String key) {
        try {
            return DefaultSplits.valueOf(key).splits;
        } catch (Exception e) {
            return DEFAULT.splits;
        }
    }

    DefaultSplits(int splits) {
        this.splits = splits;
    }

    public int getSplits() {
        return splits;
    }
}
