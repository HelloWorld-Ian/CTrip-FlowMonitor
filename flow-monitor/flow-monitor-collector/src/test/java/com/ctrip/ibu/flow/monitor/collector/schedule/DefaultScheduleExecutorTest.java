package com.ctrip.ibu.flow.monitor.collector.schedule;

import com.ctrip.ibu.flow.monitor.collector.bo.FlowReportInfo;
import com.ctrip.ibu.flow.monitor.collector.bo.SubjectInfo;
import com.ctrip.ibu.flow.monitor.collector.collect.FlowCollector;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;

/**
 * @author Ian
 * @date 2022/6/3
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultScheduleExecutorTest {

    DefaultScheduleExecutor defaultScheduleExecutor;

    @Before
    public void before() {
        defaultScheduleExecutor = DefaultScheduleExecutor.getInstance();
    }

    @Test
    public void testScheduleSampleReport() {
        SubjectInfo subjectInfo = Mockito.mock(SubjectInfo.class);
        Mockito.doReturn("subjectTest").when(subjectInfo).getName();

        FlowCollector flowCollector = Mockito.mock(FlowCollector.class);
        Mockito.doReturn(subjectInfo).when(flowCollector).getSubject();
        defaultScheduleExecutor.scheduleRefreshReport(flowCollector);

        FlowReportInfo flowReportInfo = Mockito.mock(FlowReportInfo.class);
        Mockito.doReturn(subjectInfo).when(flowReportInfo).getSubjectInfo();
        defaultScheduleExecutor.scheduleSampleReport(flowReportInfo, 1000L);

        defaultScheduleExecutor.removeSubject("subjectTest");
    }

    @Test
    public void testScheduleRefreshReport() {
        FlowCollector flowCollector = Mockito.mock(FlowCollector.class);
        SubjectInfo subjectInfo = Mockito.mock(SubjectInfo.class);
        Mockito.doReturn("subjectTest").when(subjectInfo).getName();
        Mockito.doReturn(subjectInfo).when(flowCollector).getSubject();
        defaultScheduleExecutor.scheduleRefreshReport(flowCollector);
    }

    @Test
    @SneakyThrows
    public void testExecute() {
        int[] arr = {0};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        defaultScheduleExecutor.execute(() -> {
            arr[0]++;
            countDownLatch.countDown();
        });
        countDownLatch.await();
        Assert.assertEquals(arr[0], 1);
    }

    @Test
    public void testRemoveSubject() {
        FlowCollector flowCollector = Mockito.mock(FlowCollector.class);
        SubjectInfo subjectInfo = Mockito.mock(SubjectInfo.class);
        Mockito.doReturn("subjectTest").when(subjectInfo).getName();
        Mockito.doReturn(subjectInfo).when(flowCollector).getSubject();
        defaultScheduleExecutor.scheduleRefreshReport(flowCollector);

        defaultScheduleExecutor.removeSubject("subjectTest");
    }

    @Test
    public void testGet() {
        SubjectInfo subjectInfo = Mockito.mock(SubjectInfo.class);
        Mockito.doReturn("subjectTest").when(subjectInfo).getName();

        FlowCollector flowCollector = Mockito.mock(FlowCollector.class);
        Mockito.doReturn(subjectInfo).when(flowCollector).getSubject();
        defaultScheduleExecutor.scheduleRefreshReport(flowCollector);

        FlowCollector collector = defaultScheduleExecutor.get("subjectTest");
        Assert.assertNotNull(collector);
    }
}