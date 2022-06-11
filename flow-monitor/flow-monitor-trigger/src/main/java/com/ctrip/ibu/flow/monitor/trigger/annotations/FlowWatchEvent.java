package com.ctrip.ibu.flow.monitor.trigger.annotations;

import java.lang.annotation.*;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FlowWatchEvent {
    // appId-subject-rule or subject-rule
    String[] watch() default "";
}
