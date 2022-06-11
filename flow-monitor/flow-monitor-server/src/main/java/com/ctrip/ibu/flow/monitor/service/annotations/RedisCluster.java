package com.ctrip.ibu.flow.monitor.service.annotations;

import com.ctrip.ibu.flow.monitor.service.configuration.RedisClusterRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RedisClusterRegister.class)
public @interface RedisCluster {
    String value() default "";
}
