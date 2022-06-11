package com.ctrip.ibu.flow.monitor.collector.context;

import com.ctrip.ibu.flow.monitor.collector.bo.FlowReportInfo;
import com.ctrip.ibu.flow.monitor.collector.collect.FlowCollector;
import com.ctrip.ibu.flow.monitor.collector.exception.SubjectNotFoundException;
import com.ctrip.ibu.flow.monitor.collector.qmq.DefaultFlowReportProducer;
import com.ctrip.ibu.flow.monitor.collector.qmq.FlowReportProducer;
import com.ctrip.ibu.flow.monitor.collector.schedule.DefaultScheduleExecutor;
import com.ctrip.ibu.flow.monitor.collector.schedule.ScheduleExecutor;

import java.util.List;
import java.util.Objects;


/**
 * 维护全局变量，保证单例
 *
 * @author Ian
 * @date 2022/6/3
 */
public class Context {

    private static final FlowReportProducer flowReportProducer = DefaultFlowReportProducer.getInstance();
    private static final ScheduleExecutor scheduleExecutor = DefaultScheduleExecutor.getInstance();

    /**
     * schedule executor中取出已注册的subject进行流量统计
     */
    public static void collect(String subject, String key) {
        FlowCollector collector = scheduleExecutor.get(subject);
        if (!Objects.isNull(collector)) {
            collector.collect(key);
        } else {
            throw new SubjectNotFoundException("subject %s not found", subject);
        }
    }

    public static void report(List<FlowReportInfo> info) {
        flowReportProducer.report(info);
    }

    public static void registerScheduledRefresh(FlowCollector collector) {
        scheduleExecutor.scheduleRefreshReport(collector);
    }

    public static void registerScheduledSample(FlowReportInfo flowReportInfo, long interval) {
        scheduleExecutor.scheduleSampleReport(flowReportInfo, interval);
    }

    public static void asyncExecute(Runnable runnable) {
        scheduleExecutor.execute(runnable);
    }

    public static void remove(String subject) {
        scheduleExecutor.removeSubject(subject);
    }
}
