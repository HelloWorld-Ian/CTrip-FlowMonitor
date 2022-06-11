package com.ctrip.ibu.flow.monitor.collector.qmq;

import com.ctrip.ibu.flow.monitor.collector.bo.FlowReportInfo;
import com.ctrip.ibu.flow.monitor.collector.tool.JsonUtils;
import com.google.common.collect.Lists;
import qunar.tc.qmq.Message;
import qunar.tc.qmq.producer.MessageProducerProvider;

import java.util.List;

/**
 * 流量信息上报
 *
 * @author Ian
 * @date 2022/6/3
 */
public class DefaultFlowReportProducer implements FlowReportProducer {

    private final MessageProducerProvider messageProducer = new MessageProducerProvider();
    private static volatile DefaultFlowReportProducer producer;

    private final static String SUBJECT = "ibu.market.flow.collect";
    private final static Integer MAX_MERGE = 20;

    private DefaultFlowReportProducer(){
        messageProducer.init();
    }

    public static DefaultFlowReportProducer getInstance() {
        if (producer == null) {
            synchronized (DefaultFlowReportProducer.class) {
                if (producer == null) {
                    producer = new DefaultFlowReportProducer();
                }
            }
        }
        return producer;
    }

    /**
     * 上报流量信息
     */
    @Override
    public void report(List<FlowReportInfo> reportInfo) {
        Lists.partition(reportInfo, MAX_MERGE).forEach(flowReportInfos -> {
            Message message = messageProducer.generateMessage(SUBJECT);
            message.setProperty("data", JsonUtils.toJson(flowReportInfos));
            messageProducer.sendMessage(message);
        });
    }
}
