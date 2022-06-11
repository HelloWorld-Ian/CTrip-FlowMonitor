package com.ctrip.ibu.flow.monitor.collector.schedule;

import com.ctrip.ibu.flow.monitor.collector.bo.FlowReportInfo;
import com.ctrip.ibu.flow.monitor.collector.collect.FlowCollector;
import com.ctrip.ibu.flow.monitor.collector.context.Context;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 线程池，同时执行实时任务与定时任务
 *
 * @author Ian
 * @date 2022/6/3
 */
public class DefaultScheduleExecutor implements ScheduleExecutor {
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private final Map<String/* subject name*/, CollectorWrap> collectorMap = new HashMap<>();

    // 定时上报间隔
    private final static long REPORT_INTERVAL = 10000;

    private static volatile DefaultScheduleExecutor defaultScheduleExecutor;

    private DefaultScheduleExecutor() {
        // 注册定时上报
        Runnable DEFAULT_REPORT_EVENT = () -> {
            synchronized (collectorMap) {
                for (CollectorWrap collector : collectorMap.values()) {
                    collector.getFlowCollector().report();
                }
            }
        };
        eventLoopGroup.scheduleWithFixedDelay(DEFAULT_REPORT_EVENT,
                REPORT_INTERVAL, REPORT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public static DefaultScheduleExecutor getInstance() {
        if (defaultScheduleExecutor == null) {
            synchronized (DefaultScheduleExecutor.class) {
                if (defaultScheduleExecutor == null) {
                    defaultScheduleExecutor = new DefaultScheduleExecutor();
                }
            }
        }
        return defaultScheduleExecutor;
    }

    /**
     * 注册定时订阅
     */
    @Override
    public void scheduleSampleReport(FlowReportInfo info, long interval) {
        CollectorWrap collectorWrap = collectorMap.get(info.getSubjectInfo().getName());
        if (Objects.isNull(collectorWrap)) {
            return;
        }
        ScheduledFuture<?> ret = eventLoopGroup.scheduleWithFixedDelay
                (() -> Context.report(Collections.singletonList(info)), interval, interval, TimeUnit.MILLISECONDS);
        collectorWrap.addFutures(ret);
    }

    /**
     * 注册流量上报
     */
    @Override
    public void scheduleRefreshReport(FlowCollector collector) {
        synchronized (collectorMap) {
            collectorMap.put(collector.getSubject().getName(), new CollectorWrap(collector));
        }
    }

    /**
     * 线程池执行实时任务
     */
    @Override
    public void execute(Runnable runnable) {
        eventLoopGroup.submit(runnable);
    }

    /**
     * 从线程池中移除subject定时任务（定时订阅、流量上报）
     */
    @Override
    public void removeSubject(String/* subject name */ name) {
        CollectorWrap collectorWrap = collectorMap.get(name);
        collectorMap.remove(name);
        collectorWrap.cancelSchedule();
    }

    /**
     * 从已注册subject列表中获取某一subject
     */
    @Override
    public FlowCollector get(String subject) {
        CollectorWrap collectorWrap = collectorMap.get(subject);
        if (!Objects.isNull(collectorWrap)) {
            return collectorWrap.getFlowCollector();
        }
        return null;
    }

    @Getter
    @Setter
    private static class CollectorWrap {
        private FlowCollector flowCollector;
        private List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();

        private CollectorWrap(FlowCollector flowCollector) {
            this.flowCollector = flowCollector;
        }

        private void addFutures(ScheduledFuture<?> scheduledFuture) {
            scheduledFutures.add(scheduledFuture);
        }

        private void cancelSchedule() {
            for (ScheduledFuture<?> future : scheduledFutures) {
                future.cancel(false);
            }
        }
    }
}
