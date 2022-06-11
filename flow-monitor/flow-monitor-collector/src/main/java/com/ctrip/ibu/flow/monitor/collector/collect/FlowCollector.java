package com.ctrip.ibu.flow.monitor.collector.collect;

import com.ctrip.ibu.flow.monitor.collector.bo.InstantRuleInfo;
import com.ctrip.ibu.flow.monitor.collector.bo.ScheduledRuleInfo;
import com.ctrip.ibu.flow.monitor.collector.bo.SubjectInfo;

/**
 * 流量收集器实例
 *
 * @author Ian
 * @date 2022/6/3
 */
public interface FlowCollector {
    /**
     * 设置subject信息
     */
    void setSubject(SubjectInfo subject);

    /**
     * 设置subject信息
     */
    SubjectInfo getSubject();

    /**
     * 添加实时订阅
     */
    void addScheduleRule(ScheduledRuleInfo rule);

    /**
     * 添加定时订阅
     */
    void addInstantRule(InstantRuleInfo rule);

    /**
     * 启动流量收集
     */
    void collect(String key);


    /**
     * 流量上报
     */
    void report();

    /**
     * 流量采样
     */
    void sample(String ruleName);

    /**
     * 注册，注册定时订阅，定时上报
     */
    void register();

    /**
     * 是否注册
     */
    boolean isRegistered();
}
