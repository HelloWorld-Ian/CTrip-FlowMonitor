package com.ctrip.ibu.flow.monitor.service.subject;

import com.ctrip.framework.foundation.Foundation;
import com.ctrip.ibu.flow.monitor.service.bo.*;
import com.ctrip.ibu.flow.monitor.service.rule.Rule;
import com.ctrip.ibu.flow.monitor.service.tool.redis.CRedisClient;
import credis.java.client.CacheProvider;
import credis.java.client.pipeline.CachePipeline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.Response;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSubjectTest {

    DefaultSubject subject;

    @Before
    public void before() {
        subject = new DefaultSubject(Foundation.app().getAppId(), "subjectTest", 60000L, 4);
        CacheProvider cacheProvider = Mockito.mock(CacheProvider.class);
        CachePipeline pipeline = Mockito.mock(CachePipeline.class);
        Mockito.doReturn(Mockito.mock(Response.class)).when(pipeline).incrBy(Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(Mockito.mock(Response.class)).when(pipeline).expireAt(Mockito.anyString(), Mockito.anyLong());
        Mockito.doNothing().when(pipeline).sync();
        Mockito.doReturn(pipeline).when(cacheProvider).getPipeline();
        Mockito.doReturn(1L).when(cacheProvider).ttl(Mockito.anyString());
        CRedisClient.setCacheProvider(cacheProvider);
    }

    @Test
    public void testRefresh() {
        FlowRefresh flowRefresh = buildFlowRefresh();
        List<RuleHitInform> ret = subject.refresh(flowRefresh);
        Assert.assertNotNull(ret);
    }

    @Test
    public void testSample() {
        FlowSample flowSample = buildFlowSample();
        List<RuleHitInform> ret = subject.sample(flowSample);
        Assert.assertNotNull(ret);
    }

    @Test
    public void testCheckScheduledRule() {
        Rule rule = RuleReportInfo.builder()
                .interval(10000L)
                .name("ruleName")
                .strategy("MORE_THAN")
                .threshold(1000L).build().convert();
        subject.checkScheduledRule(rule, 1000L);
    }

    public FlowRefresh buildFlowRefresh() {
        Rule rule = RuleReportInfo.builder()
                .name("ruleName")
                .strategy("MORE_THAN")
                .threshold(0L).build().convert();

        SubjectInfo subjectInfo = SubjectInfo.builder()
                .appId("123456")
                .name("subjectTest")
                .span(60000L).build();

        return FlowRefresh.builder()
                .key("testKey")
                .count(1L)
                .subjectInfo(subjectInfo)
                .rules(Collections.singletonList(rule))
                .timestamp(System.currentTimeMillis()).build();
    }

    public FlowSample buildFlowSample() {
        return FlowSample.builder()
                .rule(RuleReportInfo.builder()
                        .interval(10000L)
                        .name("ruleName")
                        .strategy("MORE_THAN")
                        .threshold(1000L).build().convert()).build();
    }
}