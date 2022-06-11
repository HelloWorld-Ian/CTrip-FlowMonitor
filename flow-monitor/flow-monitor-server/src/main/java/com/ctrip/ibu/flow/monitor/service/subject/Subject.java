package com.ctrip.ibu.flow.monitor.service.subject;

import com.ctrip.ibu.flow.monitor.service.bo.FlowRefresh;
import com.ctrip.ibu.flow.monitor.service.bo.FlowSample;
import com.ctrip.ibu.flow.monitor.service.bo.RuleHitInform;
import com.ctrip.ibu.flow.monitor.service.rule.Rule;

import java.util.List;

/**
 * @author Ian
 * @date 2022/6/3
 */
public interface Subject {
    /**
     * 刷新流量并进行规则判定
     */
    List<RuleHitInform> refresh(FlowRefresh info);

    /**
     * 对所有计数器进行流量采样并判定规则
     */
    List<RuleHitInform> sample(FlowSample info);

    /**
     * 避免定时订阅重复触发
     */
    boolean checkScheduledRule(Rule rule, long interval);
}
