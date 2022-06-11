package com.ctrip.ibu.flow.monitor.service.service;

import com.ctrip.framework.foundation.Foundation;
import com.ctrip.ibu.flow.monitor.service.bo.*;
import com.ctrip.ibu.flow.monitor.service.qmq.RuleHitInformProvider;
import com.ctrip.ibu.flow.monitor.service.rule.Rule;
import com.ctrip.ibu.flow.monitor.service.subject.Subject;
import com.ctrip.ibu.flow.monitor.service.subject.factory.Factory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FlowHandleServiceTest {

    @InjectMocks
    FlowHandleService flowHandleService;

    @Mock
    private Factory subjectFactory;

    @Mock
    private RuleHitInformProvider ruleHitInformProvider;

    @Before
    public void before() {
        RuleHitInform ruleHitInform = Mockito.mock(RuleHitInform.class);
        Subject subject = Mockito.mock(Subject.class);
        doReturn(Collections.singletonList(ruleHitInform)).when(subject).sample(any(FlowSample.class));
        doReturn(Collections.singletonList(ruleHitInform)).when(subject).refresh(any(FlowRefresh.class));
        doReturn(true).when(subject).checkScheduledRule(any(Rule.class), anyLong());
        doReturn(subject).when(subjectFactory).get(any(SubjectInfo.class));
        doNothing().when(ruleHitInformProvider).send(anyListOf(RuleHitInform.class));
    }

    @Test
    public void testDoRefresh() {
        FlowRefresh flowRefresh = buildFlowRefresh();
        flowHandleService.doRefresh(flowRefresh);
        verify(ruleHitInformProvider).send(anyListOf(RuleHitInform.class));
    }

    @Test
    public void testDoSample() {
        FlowSample flowSample = buildFlowSample();
        flowHandleService.doSample(flowSample);
        verify(ruleHitInformProvider).send(anyListOf(RuleHitInform.class));
    }

    public SubjectInfo buildSubjectInfo() {
        return SubjectInfo.builder()
                .name("subjectTest")
                .appId(Foundation.app().getAppId())
                .splits(5)
                .timeUnit("SECOND")
                .span(5L).build();
    }

    public FlowRefresh buildFlowRefresh() {
        RuleReportInfo ruleReportInfo = new RuleReportInfo();
        ruleReportInfo.setName("testRule");
        ruleReportInfo.setThreshold(100L);
        ruleReportInfo.setStrategy("MORE_THAN");
        return FlowRefresh.builder()
                .subjectInfo(buildSubjectInfo())
                .count(10L)
                .key("test")
                .timestamp(System.currentTimeMillis())
                .rules(Collections.singletonList(ruleReportInfo.convert())).build();
    }

    public FlowSample buildFlowSample() {
        RuleReportInfo ruleReportInfo = new RuleReportInfo();
        ruleReportInfo.setName("testRule");
        ruleReportInfo.setInterval(10000L);
        ruleReportInfo.setThreshold(100L);
        ruleReportInfo.setStrategy("MORE_THAN");
        return FlowSample.builder()
                .subjectInfo(buildSubjectInfo())
                .rule(ruleReportInfo.convert())
                .interval(1000L).build();
    }
}