package com.ctrip.ibu.flow.monitor.collector.qmq;

import com.ctrip.ibu.flow.monitor.collector.bo.FlowReportInfo;

import java.util.List;

/**
 * 流量上报
 *
 * @author Ian
 * @date 2022/6/3
 */
public interface FlowReportProducer {
    void report(List<FlowReportInfo> reportInfo);
}
