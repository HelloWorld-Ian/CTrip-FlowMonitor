package com.ctrip.ibu.flow.monitor.collector.aop;

import com.ctrip.ibu.flow.monitor.collector.annotations.FlowMonitor;
import lombok.Builder;
import lombok.Getter;
import org.aspectj.lang.JoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class FlowMonitorAspectTest {

    @Spy
    FlowMonitorAspect flowMonitorAspect;

    @Test
    public void testInvoke() {
        Info info1 = Info.builder()
                .name("Ian").build();
        Info info2 = Info.builder()
                .name("Be").build();
        TestRequest request = TestRequest.builder()
                .id("1")
                .info(Arrays.asList(info1, info2)).build();
        FlowMonitor flowMonitor = Mockito.mock(FlowMonitor.class);
        Mockito.doReturn("subjectTest").when(flowMonitor).subject();
        Mockito.doReturn("${0:info/0/name}-${0:id}-${0}").when(flowMonitor).key();
        JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
        Mockito.doReturn(new Object[] {request}).when(joinPoint).getArgs();
        flowMonitorAspect.collect(joinPoint, flowMonitor);
    }


    @Getter
    @Builder
    private static class TestRequest {
        List<Info> info;
        String id;
    }

    @Getter
    @Builder
    private static class Info {
        String name;
    }
}