package com.ctrip.ibu.flow.monitor.collector.context;

import com.ctrip.ibu.flow.monitor.collector.bo.*;
import com.ctrip.ibu.flow.monitor.collector.collect.DefaultFlowCollector;
import com.ctrip.ibu.flow.monitor.collector.collect.FlowCollector;
import com.ctrip.ibu.flow.monitor.collector.exception.SubjectNotFoundException;
import junit.framework.TestCase;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

/**
 * @author Ian
 * @date 2022/6/3
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextTest extends TestCase {

    @Spy
    DefaultFlowCollector defaultFlowCollector;


    @Test(expected = SubjectNotFoundException.class)
    public void testCollectSubjectNotFound() {
        Context.collect("subjectTest1", "keyTest");
    }

    @Test
    public void testCollect() {
        InstantRuleInfo info = new InstantRuleInfo();
        info.setThreshold(1000L);
        info.setStrategy("MORE_THAN");
        info.setName("ruleTest");
        defaultFlowCollector.addInstantRule(info);

        ScheduledRuleInfo info2 = new ScheduledRuleInfo();
        info2.setThreshold(1000L);
        info2.setStrategy("MORE_THAN");
        info2.setName("ruleTest");
        info2.setInterval(100000L);
        info2.setTimeUnit("SECOND");
        defaultFlowCollector.addScheduleRule(info2);

        SubjectInfo subjectInfo = new SubjectInfo();
        subjectInfo.setAppId("200020210");
        subjectInfo.setSpan(10000L);
        subjectInfo.setName("subjectTest");
        subjectInfo.setSplits(5);
        subjectInfo.setTimeUnit("SECOND");
        defaultFlowCollector.setSubject(subjectInfo);

        defaultFlowCollector.register();

        Context.collect("subjectTest","keyTest");
    }

    @Test
    public void testReport() {
        FlowReportInfo flowReportInfo = new FlowReportInfo();

        SubjectInfo subjectInfo = new SubjectInfo();
        subjectInfo.setSplits(5);
        subjectInfo.setSpan(1000L);
        subjectInfo.setTimeUnit("SECOND");
        subjectInfo.setName("subjectTest");
        subjectInfo.setAppId("200020210");

        flowReportInfo.setSubjectInfo(subjectInfo);
        flowReportInfo.setCount(100L);
        flowReportInfo.setKey("testKey");
        flowReportInfo.setType("refresh");
        flowReportInfo.setTimestamp(System.currentTimeMillis());

        RuleReportInfo ruleReportInfo = new RuleReportInfo();
        ruleReportInfo.setName("ruleTest");
        ruleReportInfo.setInterval(1000L);
        ruleReportInfo.setStrategy("MORE_THAN");
        ruleReportInfo.setThreshold(100L);

        flowReportInfo.setRules(Collections.singletonList(ruleReportInfo));

        Context.report(Collections.singletonList(flowReportInfo));
    }

    @Test
    public void testRegisterScheduledRefresh() {
        FlowCollector flowCollector = Mockito.mock(FlowCollector.class);
        SubjectInfo subjectInfo = Mockito.mock(SubjectInfo.class);
        Mockito.doReturn("subjectTest").when(subjectInfo).getName();
        Mockito.doReturn(subjectInfo).when(flowCollector).getSubject();

        Context.registerScheduledRefresh(flowCollector);
    }

    @Test
    public void testRegisterScheduledSample() {
        SubjectInfo subjectInfo = Mockito.mock(SubjectInfo.class);
        Mockito.doReturn("subjectTest").when(subjectInfo).getName();

        FlowCollector flowCollector = Mockito.mock(FlowCollector.class);
        Mockito.doReturn(subjectInfo).when(flowCollector).getSubject();
        Context.registerScheduledRefresh(flowCollector);

        FlowReportInfo flowReportInfo = Mockito.mock(FlowReportInfo.class);
        Mockito.doReturn(subjectInfo).when(flowReportInfo).getSubjectInfo();
        Context.registerScheduledSample(flowReportInfo, 10000L);
        Context.remove("subjectTest");
    }

    @Test
    @SneakyThrows
    public void testSyncExecute() {
        int[] a = {0};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Context.asyncExecute(() -> {
            a[0]++;
            countDownLatch.countDown();
        });
        countDownLatch.await();
        Assert.assertEquals(a[0], 1);
    }
}