package com.ctrip.ibu.flow.monitor.collector.qmq;

import com.ctrip.ibu.flow.monitor.collector.bo.FlowReportInfo;
import com.ctrip.ibu.flow.monitor.collector.bo.RuleReportInfo;
import com.ctrip.ibu.flow.monitor.collector.bo.SubjectInfo;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author Ian
 * @date 2022/6/3
 */
public class DefaultFlowReportProducerTest {

    DefaultFlowReportProducer producer;

    @Before
    public void before() {
        producer = DefaultFlowReportProducer.getInstance();
    }

    @Test
    public void testReport() {
        List<FlowReportInfo> flowReportInfos = Collections.singletonList(buildFlowReportInfo());
        producer.report(flowReportInfos);
    }

    public FlowReportInfo buildFlowReportInfo() {
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
        return flowReportInfo;
    }
}