package com.ctrip.ibu.flow.monitor.collector.bo.qconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubjectConfig {
    String name;
    String timeUnit;
    // 流量统计时间窗
    Long span;
    Integer splits;
    // 实时订阅规则
    List<RuleConfig> instantSubscribe;
    // 定时订阅规则
    List<RuleConfig> scheduleSubscribe;
}
