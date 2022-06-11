package com.ctrip.ibu.flow.monitor.service.qmq;

import com.ctrip.ibu.flow.monitor.service.bo.RuleHitInform;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRuleHitInformProviderTest {

    @InjectMocks
    DefaultRuleHitInformProvider ruleHitInformProvider;

    @Before
    public void before() {
        ruleHitInformProvider.producer.init();
    }

    @Test
    public void testSend() {
        List<RuleHitInform> ruleHitInforms = Collections.singletonList(buildRuleHitInform());
        ruleHitInformProvider.send(ruleHitInforms);
    }

    public RuleHitInform buildRuleHitInform() {
        return RuleHitInform.builder()
                .appId("200020210")
                .count(1000L)
                .increment(40L)
                .key("testKey")
                .ruleName("testRule")
                .span(1000L)
                .timestamp(System.currentTimeMillis())
                .subject("testSubject").build();
    }
}