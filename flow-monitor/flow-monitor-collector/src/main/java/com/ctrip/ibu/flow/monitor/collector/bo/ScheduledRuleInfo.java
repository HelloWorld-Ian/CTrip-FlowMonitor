package com.ctrip.ibu.flow.monitor.collector.bo;

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
public class ScheduledRuleInfo {
    private String name;
    private String strategy;
    private String timeUnit;
    private Long threshold;
    private Long interval;
}
