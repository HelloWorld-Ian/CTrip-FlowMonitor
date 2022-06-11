package com.ctrip.ibu.flow.monitor.service;

import com.ctrip.ibu.flow.monitor.service.annotations.RedisCluster;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = "com.ctrip.ibu.flow.monitor")
@RedisCluster("ibu_thumb_lottery")
public class ServiceInitializer extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(ServiceInitializer.class, args);
    }
}
