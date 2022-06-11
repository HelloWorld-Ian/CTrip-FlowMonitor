package com.ctrip.ibu.flow.monitor.service.rule;


import com.ctrip.ibu.flow.monitor.service.bo.FlowResult;

/**
 * 规则
 *
 * @author Ian
 * @date 2022/6/3
 */
public interface Rule {
    /**
     * 判断规则是否命中
     */
    boolean judge(FlowResult result);

    /**
     * 获取rule name
     */
    String getName();

    /**
     * 规则策略名称
     */
    String getStrategyName();
}
