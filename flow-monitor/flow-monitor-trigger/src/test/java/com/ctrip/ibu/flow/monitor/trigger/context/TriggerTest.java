package com.ctrip.ibu.flow.monitor.trigger.context;

import org.junit.Test;


public class TriggerTest {

    @Test
    public void testWatch() {
        Trigger.watch("200020210-subjectTest-ruleTest", flowInfo -> {});
    }
}