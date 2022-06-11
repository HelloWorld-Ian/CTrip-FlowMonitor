package com.ctrip.ibu.flow.monitor.service.qmq;

import com.ctrip.framework.foundation.Foundation;
import com.ctrip.ibu.flow.monitor.service.bo.*;
import com.ctrip.ibu.flow.monitor.service.service.FlowHandleService;
import com.ctrip.ibu.flow.monitor.service.tool.json.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.mockito.runners.MockitoJUnitRunner;
import qunar.tc.qmq.Message;
import qunar.tc.qmq.producer.MessageProducerProvider;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFlowInfoConsumerTest {

    @InjectMocks
    DefaultFlowInfoConsumer flowInfoConsumer;

    @Mock
    FlowHandleService flowHandleService;

    MessageProducerProvider messageProducerProvider;

    @Before
    public void before() {
        messageProducerProvider = new MessageProducerProvider();
        messageProducerProvider.init();

        doNothing().when(flowHandleService).doRefresh(any(FlowRefresh.class));
        doNothing().when(flowHandleService).doSample(any(FlowSample.class));
    }

    @Test
    public void testHandle() {
        MessageProducerProvider messageProducerProvider = new MessageProducerProvider();
        messageProducerProvider.init();

        // test refresh
        flowInfoConsumer.handle(flowRefreshMsg());
        Mockito.verify(flowHandleService, new Times(1)).doRefresh(any(FlowRefresh.class));

        // test sample
        flowInfoConsumer.handle(flowSampleMsg());
        Mockito.verify(flowHandleService, new Times(1)).doSample(any(FlowSample.class));
    }

    public Message flowRefreshMsg() {
        SubjectInfo subjectInfo = SubjectInfo.builder()
                .appId(Foundation.app().getAppId())
                .name("subjectTest")
                .span(100L)
                .timeUnit("SECOND")
                .splits(5).build();

        RuleReportInfo ruleReportInfo = new RuleReportInfo();
        ruleReportInfo.setName("ruleTest");
        ruleReportInfo.setStrategy("MORE_THAN");
        ruleReportInfo.setThreshold(1L);

        FlowReportInfo flowReportInfo = FlowReportInfo.builder()
                .count(10L)
                .key("test")
                .timestamp(System.currentTimeMillis())
                .type("refresh")
                .subjectInfo(subjectInfo)
                .rules(Collections.singletonList(ruleReportInfo)).build();

        Message message = messageProducerProvider.generateMessage("ibu.market.flow.collect");
        message.setProperty("data", JsonUtils.toJson(Collections.singletonList(flowReportInfo)));
        return message;
    }

    public Message flowSampleMsg() {
        SubjectInfo subjectInfo = SubjectInfo.builder()
                .appId(Foundation.app().getAppId())
                .name("subjectTest")
                .span(100L)
                .timeUnit("SECOND")
                .splits(5).build();

        RuleReportInfo ruleReportInfo = new RuleReportInfo();
        ruleReportInfo.setName("ruleTest");
        ruleReportInfo.setStrategy("MORE_THAN");
        ruleReportInfo.setThreshold(1L);
        ruleReportInfo.setInterval(4000L);

        FlowReportInfo flowReportInfo = FlowReportInfo.builder()
                .count(10L)
                .key("test")
                .timestamp(System.currentTimeMillis())
                .type("sample")
                .subjectInfo(subjectInfo)
                .rules(Collections.singletonList(ruleReportInfo)).build();

        Message message = messageProducerProvider.generateMessage("ibu.market.flow.collect");
        message.setProperty("data", JsonUtils.toJson(Collections.singletonList(flowReportInfo)));
        return message;
    }
}