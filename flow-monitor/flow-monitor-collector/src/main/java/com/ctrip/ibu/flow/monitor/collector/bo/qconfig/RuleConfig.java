package com.ctrip.ibu.flow.monitor.collector.bo.qconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RuleConfig {
    String name;
    // 订阅策略 MORE_THAN / LESS_THAN / UP_THROUGH / DOWN_THROUGH
    String strategy;
    // 阈值
    Long threshold;

    // 定时订阅间隔
    Long interval;
    String timeUnit;
}
