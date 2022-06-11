package com.ctrip.ibu.flow.monitor.collector.collect;

import com.ctrip.ibu.flow.monitor.collector.bo.InstantRuleInfo;
import com.ctrip.ibu.flow.monitor.collector.bo.ScheduledRuleInfo;
import com.ctrip.ibu.flow.monitor.collector.bo.SubjectInfo;
import com.ctrip.ibu.flow.monitor.collector.bo.qconfig.RuleConfig;
import com.ctrip.ibu.flow.monitor.collector.bo.qconfig.SubjectConfig;
import com.ctrip.ibu.flow.monitor.collector.context.Context;
import com.ctrip.ibu.flow.monitor.collector.tool.JsonUtils;
import com.ctrip.framework.foundation.Foundation;
import qunar.tc.qconfig.client.JsonConfig;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流量收集入口
 *
 * @author Ian
 * @date 2022/6/3
 */
public class FlowCollectorFactory {

    /**
     * 本地缓存
     */
    private final static Map<String/* subject-key */, CollectorWrap> collectorMap = new ConcurrentHashMap<>();

    static {
        JsonConfig.ParameterizedClass param = JsonConfig.ParameterizedClass.of(List.class, SubjectConfig.class);
        JsonConfig<List<SubjectConfig>> subjectConfigs = JsonConfig.get("flow-monitor-subject.json", param);
        List<SubjectConfig> current = subjectConfigs.current();
        // 第一次同步qconfig配置时全部注册
        checkConfig(current);
        subjectConfigs.addListener(FlowCollectorFactory::checkConfig);
    }

    private FlowCollectorFactory(){}

    /**
     * 流量收集入口
     */
    public static void doCollect(String subject, String key) {
        Context.collect(subject, key);
    }

    /**
     * 检查初始化或重置
     */
    private static void checkConfig(List<SubjectConfig> conf) {
        for (SubjectConfig subjectConfig : conf) {
            if (!collectorMap.containsKey(subjectConfig.getName())) {
                CollectorWrap collectorWrap = new CollectorWrap(subjectConfig);
                collectorMap.put(subjectConfig.getName(), collectorWrap);
            } else {
                CollectorWrap collectorWrap = collectorMap.get(subjectConfig.getName());
                collectorWrap.refreshOnChange(subjectConfig);
            }
        }
    }


    private static class CollectorWrap {
        private SubjectConfig subjectConfig;
        private String subjectConfigStr;

        private CollectorWrap(SubjectConfig/* not null */ config) {
            this.subjectConfig = config;
            this.subjectConfigStr = JsonUtils.toJson(this.subjectConfig);
            init();
        }

        private void refreshOnChange(SubjectConfig subjectConfig) {
            String curConfigStr = JsonUtils.toJson(subjectConfig);
            if (!Objects.equals(subjectConfigStr, curConfigStr)) {
                clear();
                this.subjectConfig = subjectConfig;
                this.subjectConfigStr = JsonUtils.toJson(this.subjectConfig);
                init();
            }
        }

        /**
         * subject实例初始化
         */
        private void init() {
            SubjectInfo subjectInfo = new SubjectInfo();
            subjectInfo.setName(subjectConfig.getName());
            subjectInfo.setSpan(subjectConfig.getSpan());
            subjectInfo.setTimeUnit(subjectConfig.getTimeUnit());
            subjectInfo.setSplits(subjectConfig.getSplits());
            subjectInfo.setAppId(Foundation.app().getAppId());

            FlowCollector flowCollector = new DefaultFlowCollector();
            flowCollector.setSubject(subjectInfo);
            // 注册实时订阅
            List<RuleConfig> instantRule =  subjectConfig.getInstantSubscribe();
            if (!Objects.isNull(instantRule)) {
                for (RuleConfig ruleConfig : instantRule) {
                    InstantRuleInfo instantRuleInfo = new InstantRuleInfo();
                    instantRuleInfo.setName(ruleConfig.getName());
                    instantRuleInfo.setStrategy(ruleConfig.getStrategy());
                    instantRuleInfo.setThreshold(ruleConfig.getThreshold());
                    flowCollector.addInstantRule(instantRuleInfo);
                }
            }

            List<RuleConfig> scheduleRule = subjectConfig.getScheduleSubscribe();
            if (!Objects.isNull(scheduleRule)) {
                for (RuleConfig ruleConfig : scheduleRule) {
                    ScheduledRuleInfo scheduledRuleInfo = new ScheduledRuleInfo();
                    scheduledRuleInfo.setName(ruleConfig.getName());
                    scheduledRuleInfo.setInterval(ruleConfig.getInterval());
                    scheduledRuleInfo.setStrategy(ruleConfig.getStrategy());
                    scheduledRuleInfo.setThreshold(ruleConfig.getThreshold());
                    scheduledRuleInfo.setTimeUnit(ruleConfig.getTimeUnit());
                    flowCollector.addScheduleRule(scheduledRuleInfo);
                }
            }

            // 注册定时任务
            flowCollector.register();
            // 缓存管理
            collectorMap.put(subjectConfig.getName(), this);
        }

        /**
         * 解除定时任务
         */
        private void clear() {
            String subjectName = subjectConfig.getName();
            Context.remove(subjectName);
        }
    }
}
