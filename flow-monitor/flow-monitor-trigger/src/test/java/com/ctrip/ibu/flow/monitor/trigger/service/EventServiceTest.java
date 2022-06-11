package com.ctrip.ibu.flow.monitor.trigger.service;

import com.ctrip.ibu.flow.monitor.trigger.bo.RuleHitInform;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

/**
 * @author Ian
 * @date 2022/6/4
 */
public class EventServiceTest {

    EventService eventService = EventService.getInstance();

    @Test
    @SneakyThrows
    public void testInvoke() {
        RuleHitInform ruleHitInform = RuleHitInform.builder()
                .appId("200020210")
                .subject("subjectTest")
                .ruleName("ruleTest")
                .count(1000L)
                .key("keyTest")
                .timestamp(System.currentTimeMillis())
                .increment(5L)
                .span(10000L).build();
        int[] arr = {0};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        eventService.watch("200020210-subjectTest-ruleTest", flowInfo -> {
            arr[0]++;
            countDownLatch.countDown();
        });
        eventService.invoke(Collections.singletonList(ruleHitInform));
        countDownLatch.await();
        Assert.assertEquals(1, arr[0]);
    }
}