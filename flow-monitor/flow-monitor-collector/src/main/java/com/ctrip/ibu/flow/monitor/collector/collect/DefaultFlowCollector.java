package com.ctrip.ibu.flow.monitor.collector.collect;

import com.ctrip.ibu.flow.monitor.collector.bo.*;
import com.ctrip.ibu.flow.monitor.collector.constant.TimeConvert;
import com.ctrip.ibu.flow.monitor.collector.context.Context;
import lombok.Getter;
import lombok.Setter;
import qunar.tc.qmq.MessageProducer;
import qunar.tc.qmq.producer.MessageProducerProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Getter
@Setter
public class DefaultFlowCollector implements FlowCollector {

    private final List<RuleReportInfo> instantRuleInfos = new ArrayList<>();
    private final Map<String, RuleReportInfo> scheduledRuleInfos = new ConcurrentHashMap<>();
    private final Map<String, Long> counts = new ConcurrentHashMap<>();
    private final MessageProducer flowReportProducer = new MessageProducerProvider();
    private SubjectInfo subject;

    private boolean isScheduled = false;

    /**
     * 缓存key的上限
     */
    private final static Long CAPACITY = 5000L;
    private final Lock lock = new ReentrantLock();

    /**
     * 添加实时订阅规则
     */
    @Override
    public void addInstantRule(InstantRuleInfo rule) {
        RuleReportInfo ruleReportInfo = RuleReportInfo.builder()
                .name(rule.getName())
                .strategy(rule.getStrategy())
                .threshold(rule.getThreshold()).build();
        instantRuleInfos.add(ruleReportInfo);
    }

    /**
     * 添加定时订阅规则
     */
    @Override
    public void addScheduleRule(ScheduledRuleInfo rule) {
        String timeUnit = rule.getTimeUnit();
        long interval = rule.getInterval();
        interval *= TimeConvert.convert(timeUnit);
        RuleReportInfo ruleReportInfo = RuleReportInfo.builder()
                .name(rule.getName())
                .threshold(rule.getThreshold())
                .strategy(rule.getStrategy())
                .interval(interval).build();
        scheduledRuleInfos.put(ruleReportInfo.getName(), ruleReportInfo);
    }

    /**
     * 收集流量
     * @param key 流量对应的key
     */
    @Override
    public void collect(String key) {
        counts.put(key, counts.getOrDefault(key, 0L) + 1);
        if (counts.size() > CAPACITY) {
            Context.asyncExecute(this::report);
        }
    }

    /**
     * 上报流量
     */
    @Override
    public void report() {
        try {
            lock.lock();
            List<FlowReportInfo> flowReportInfo = new ArrayList<>();
            for (Map.Entry<String, Long> entry : counts.entrySet()) {
                flowReportInfo.add(buildRefreshReport(entry.getKey(), entry.getValue()));
            }
            Context.report(flowReportInfo);
            counts.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 流量采样
     */
    @Override
    public void sample(String ruleName) {
        try {
            lock.lock();
            RuleReportInfo ruleReportInfo = scheduledRuleInfos.get(ruleName);
            if (Objects.isNull(ruleReportInfo)) {
                return;
            }
            List<FlowReportInfo> reportInfos = Collections.singletonList(buildSampleReport(ruleReportInfo));
            Context.report(reportInfos);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 注册定时订阅，定时上报
     */
    @Override
    public void register() {
        try {
            lock.lock();
            if (isScheduled) {
                return;
            }
            Context.registerScheduledRefresh(this);
            for (RuleReportInfo ruleReportInfo : scheduledRuleInfos.values()) {
                Context.registerScheduledSample(buildSampleReport(ruleReportInfo), ruleReportInfo.getInterval());
            }
            isScheduled = true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 判断是否注册
     */
    @Override
    public boolean isRegistered() {
        return isScheduled;
    }

    private FlowReportInfo buildRefreshReport(String key, Long count) {
        return FlowReportInfo.builder()
                .key(key)
                .type("refresh")
                .subjectInfo(subject)
                .rules(instantRuleInfos)
                .count(count)
                .timestamp(System.currentTimeMillis()).build();
    }

    private FlowReportInfo buildSampleReport(RuleReportInfo ruleReportInfo) {
        return FlowReportInfo.builder()
                .type("sample")
                .subjectInfo(subject)
                .rules(Collections.singletonList(ruleReportInfo))
                .timestamp(System.currentTimeMillis()).build();
    }
}
