package com.ctrip.ibu.flow.monitor.collector.annotations;

import java.lang.annotation.*;

/**
 * 基于注解实现流量监控
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FlowMonitor {
    String subject();
    String key();
}
