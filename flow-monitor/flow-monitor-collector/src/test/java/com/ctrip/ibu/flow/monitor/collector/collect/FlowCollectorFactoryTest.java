package com.ctrip.ibu.flow.monitor.collector.collect;

import junit.framework.TestCase;

public class FlowCollectorFactoryTest extends TestCase {

    public void testDoCollect() {
        FlowCollectorFactory.doCollect("subjectTest", "test");
    }
}