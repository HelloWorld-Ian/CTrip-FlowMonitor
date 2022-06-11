package com.ctrip.ibu.flow.monitor.service.subject;

import com.ctrip.ibu.flow.monitor.service.bo.*;
import com.ctrip.ibu.flow.monitor.service.counter.Counter;
import com.ctrip.ibu.flow.monitor.service.tool.json.JsonUtils;
import com.ctrip.ibu.flow.monitor.service.tool.log.LogAction;
import com.ctrip.ibu.flow.monitor.service.tool.log.MetricLog;
import com.ctrip.ibu.flow.monitor.service.tool.redis.CRedisClient;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import credis.java.client.CacheProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.ctrip.ibu.flow.monitor.service.counter.DefaultCRedisCounter;
import com.ctrip.ibu.flow.monitor.service.rule.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Slf4j
@Getter
@Setter
public class DefaultSubject implements Subject {

    List<Rule> fixedRules = new ArrayList<>();
    List<Rule> scheduledRules = new ArrayList<>();

    private String name;
    private String appId;
    private long span = 1000;
    private int splits = 2;

    /**
     * counter缓存
     */
    private LoadingCache<String, Counter> counterCache;

    public DefaultSubject(String appId, String name) {
        this.name = name;
        this.appId = appId;
        initCache();
    }

    public DefaultSubject(String appId, String name, long span, int splits) {
        this.name = name;
        this.appId = appId;
        this.span = span;
        this.splits = splits;
        initCache();
    }

    /**
     * 初始化缓存
     */
    public void initCache() {
        long expire = span;
        counterCache = CacheBuilder.newBuilder()
                .expireAfterAccess(expire, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, Counter>() {
                    @Override
                    public Counter load(String key) {
                        return initCounter(key);
                    }
                });
    }

    /**
     * 初始化计数器
     */
    private Counter initCounter(String key) {
        return new DefaultCRedisCounter
                (String.format("%s-%s", appId, name), key, span, splits);
    }

    /**
     * 刷新流量并进行规则判定
     */
    @Override
    public List<RuleHitInform> refresh(FlowRefresh info) {
        Counter counter;
        try {
            counter = counterCache.get(info.getKey());
        } catch (ExecutionException e) {
            log.error("get counter cache fail", e);
            return new ArrayList<>();
        }
        FlowResult flowResult = counter.incr(info.getCount(), info.getTimestamp());
        List<RuleHitInform> ret = judgeRules(Collections.singletonList(flowResult), info.getRules());
        // 规则命中后埋点
        if (!ret.isEmpty()) {
            SubjectInfo subjectInfo = info.getSubjectInfo();
            for (RuleHitInform ruleHitInform : ret) {
                MetricLog.createInstant(LogAction.PROCESS)
                        .logIndexTag(MetricLog.MetricIndex.appId.name(), subjectInfo.getAppId())
                        .logIndexTag(MetricLog.MetricIndex.subjectName.name(), subjectInfo.getName())
                        .logIndexTag(MetricLog.MetricIndex.ruleName.name(), ruleHitInform.getRuleName())
                        .logIndexTag(MetricLog.MetricIndex.timestamp.name(), Long.toString(System.currentTimeMillis()))
                        .send("ruleHitInfo");
            }
        }
        return ret;
    }

    /**
     * 对所有计数器进行流量采样并判定规则
     */
    @Override
    public List<RuleHitInform> sample(FlowSample info) {
        List<FlowResult> res = new ArrayList<>();
        for (Counter counter : counterCache.asMap().values()) {
            FlowResult flowResult = counter.current();
            res.add(flowResult);
        }
        List<RuleHitInform> ret = judgeRules(res, Collections.singletonList(info.getRule()));
        // 规则命中后埋点
        if (!ret.isEmpty()) {
            SubjectInfo subjectInfo = info.getSubjectInfo();
            MetricLog.createInstant(LogAction.PROCESS)
                    .logIndexTag(MetricLog.MetricIndex.appId.name(), subjectInfo.getAppId())
                    .logIndexTag(MetricLog.MetricIndex.subjectName.name(), subjectInfo.getName())
                    .logIndexTag(MetricLog.MetricIndex.ruleName.name(), info.getRule().getName())
                    .logIndexTag(MetricLog.MetricIndex.timestamp.name(), Long.toString(System.currentTimeMillis()))
                    .send("ruleHitInfo");
        }
        return ret;
    }

    /**
     * 避免定时订阅重复触发
     */
    @Override
    public boolean checkScheduledRule(Rule rule, long interval) {
        // ms convert to s
        interval = interval/1000;
        CacheProvider provider = CRedisClient.getCacheProvider();
        return provider.set(String.format("%s-%s-%s", appId, name, rule.getName()),
                "1", "NX", "EX", interval);
    }

    /**
     * 规则判定
     */
    private List<RuleHitInform> judgeRules(List<FlowResult> flowResults, List<Rule> rules) {
        if (Objects.isNull(flowResults) || Objects.isNull(rules)) {
            return new ArrayList<>();
        }
        List<RuleHitInform> ret = new ArrayList<>();
        for (Rule rule : rules) {
            for (FlowResult res : flowResults) {
                if (rule.judge(res)) {
                    RuleHitInform ruleHitInform = RuleHitInform.builder()
                            .subject(name)
                            .appId(appId)
                            .ruleName(rule.getName())
                            .timestamp(System.currentTimeMillis())
                            .count(res.getCurCount())
                            .increment(res.getCurCount() - res.getPreCount())
                            .span(res.getEndTime() - res.getStartTime())
                            .strategy(rule.getStrategyName())
                            .key(res.getKey()).build();
                    ret.add(ruleHitInform);
                }
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        return appId + "-" + name;
    }
}
