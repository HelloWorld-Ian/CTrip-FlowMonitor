package com.ctrip.ibu.flow.monitor.collector.schedule;

import com.ctrip.ibu.flow.monitor.collector.bo.FlowReportInfo;
import com.ctrip.ibu.flow.monitor.collector.collect.FlowCollector;

/**
 * 任务执行线程池
 *
 * @author Ian
 * @date 2022/6/3
 */
public interface ScheduleExecutor {
    /**
     * 注册定时订阅任务
     */
    void scheduleSampleReport(FlowReportInfo info, long interval);

    /**
     * 注册定时上报
     */
    void scheduleRefreshReport(FlowCollector collector);

    /**
     * 执行任务
     */
    void execute(Runnable runnable);

    /**
     * 移除subject
     */
    void removeSubject(String name);

    /**
     * 获取flow collector
     */
    FlowCollector get(String subject);
}
