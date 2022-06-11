package com.ctrip.ibu.flow.monitor.service.configuration;

import com.ctrip.ibu.flow.monitor.service.annotations.RedisCluster;
import com.ctrip.ibu.flow.monitor.service.tool.redis.CRedisClient;
import credis.java.client.util.CacheFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author Ian
 * @date 2022/6/3
 */
public class RedisClusterRegister implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        Map<String,Object> attributes= annotationMetadata.getAnnotationAttributes(RedisCluster.class.getName());
        String redisClusterName = attributes.get("value").toString();
        CRedisClient.setCacheProvider(CacheFactory.getProvider(redisClusterName));
    }
}
