package com.ctrip.ibu.flow.monitor.service.rule.strategy;

import com.ctrip.ibu.flow.monitor.service.bo.FlowResult;

/**
 * 订阅策略，检查流量状态，判断规则是否命中
 *
 * @author Ian
 * @date 2022/6/3
 */
public interface Strategy {
    boolean judge(FlowResult result, long threshold);
}
