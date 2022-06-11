package com.ctrip.ibu.flow.monitor.trigger.qmq;

import com.ctrip.ibu.flow.monitor.trigger.bo.RuleHitInform;
import com.ctrip.ibu.flow.monitor.trigger.tool.JsonUtils;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import qunar.tc.qmq.Message;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRuleHitInformConsumerTest {

    RuleHitInformConsumer ruleHitInformConsumer = DefaultRuleHitInformConsumer.getInstance();

    @Test
    public void testHandle() {
        RuleHitInform ruleHitInform = RuleHitInform.builder()
                .appId("200020210")
                .subject("subjectTest")
                .ruleName("ruleTest")
                .count(1000L)
                .key("keyTest")
                .timestamp(System.currentTimeMillis())
                .increment(5L)
                .span(10000L).build();
        Message message = Mockito.mock(Message.class);
        Mockito.doReturn(JsonUtils.toJson(Collections.singletonList(ruleHitInform)))
                .when(message).getStringProperty("data");
        ruleHitInformConsumer.handle(message);
    }
}