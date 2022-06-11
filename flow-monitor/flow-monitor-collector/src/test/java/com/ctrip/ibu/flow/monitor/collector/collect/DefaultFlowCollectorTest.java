package com.ctrip.ibu.flow.monitor.collector.collect;

import com.ctrip.ibu.flow.monitor.collector.bo.InstantRuleInfo;
import com.ctrip.ibu.flow.monitor.collector.bo.ScheduledRuleInfo;
import com.ctrip.ibu.flow.monitor.collector.bo.SubjectInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Ian
 * @date 2022/6/3
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultFlowCollectorTest {

    @Spy
    DefaultFlowCollector defaultFlowCollector;

    @Before
    public void before() {
        defaultFlowCollector.setSubject(buildSubjectInfo());
    }

    @Test
    public void testCollect() {
        defaultFlowCollector.collect("testKey");
        Assert.assertFalse(defaultFlowCollector.getCounts().isEmpty());
    }

    @Test
    public void testReport() {
        defaultFlowCollector.collect("testKey");
        defaultFlowCollector.report();
        Assert.assertTrue(defaultFlowCollector.getCounts().isEmpty());
    }

    @Test
    public void testSample() {
        ScheduledRuleInfo info = new ScheduledRuleInfo();
        info.setThreshold(1000L);
        info.setStrategy("MORE_THAN");
        info.setName("ruleTest");
        info.setInterval(100000L);
        info.setTimeUnit("SECOND");
        defaultFlowCollector.addScheduleRule(info);

        defaultFlowCollector.register();
        defaultFlowCollector.sample("ruleTest");
    }

    @Test
    public void testRegister() {
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

        defaultFlowCollector.register();

        Assert.assertTrue(defaultFlowCollector.isRegistered());
    }

    @Test
    public void testGetSubject() {
        SubjectInfo subjectInfo = defaultFlowCollector.getSubject();
        Assert.assertNotNull(subjectInfo);
    }

    @Test
    public void testSetSubject() {
        defaultFlowCollector.setSubject(buildSubjectInfo());
    }

    public SubjectInfo buildSubjectInfo() {
        SubjectInfo subjectInfo = new SubjectInfo();
        subjectInfo.setAppId("200020210");
        subjectInfo.setSpan(10000L);
        subjectInfo.setName("subjectTest");
        subjectInfo.setSplits(5);
        subjectInfo.setTimeUnit("SECOND");
        return subjectInfo;
    }
}