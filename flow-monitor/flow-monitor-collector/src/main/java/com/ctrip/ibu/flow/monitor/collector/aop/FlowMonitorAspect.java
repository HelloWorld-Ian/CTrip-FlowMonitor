package com.ctrip.ibu.flow.monitor.collector.aop;

import com.ctrip.ibu.flow.monitor.collector.annotations.FlowMonitor;
import com.ctrip.ibu.flow.monitor.collector.collect.FlowCollectorFactory;
import com.ctrip.ibu.flow.monitor.collector.tool.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
public class FlowMonitorAspect {

    private final static Pattern regex = Pattern.compile("\\$\\{([^}]*)}");

    @Pointcut("@annotation(com.ctrip.ibu.flow.monitor.collector.annotations.FlowMonitor)")
    public void pointCut() {}

    @Before("@annotation(flowMonitor)")
    public void collect(JoinPoint joinPoint, FlowMonitor flowMonitor) {
        try {
            Object[] args = joinPoint.getArgs();
            JsonNode node = JsonUtils.readTree(JsonUtils.toJson(args));
            String fields = flowMonitor.key();
            Matcher matcher = regex.matcher(fields);
            StringBuffer key = new StringBuffer();
            while (matcher.find()) {
                String field = matcher.group(1);
                if (field.matches("^[0-9]+$")) {
                    field = String.format("/%s", Integer.parseInt(field));
                } else {
                    String[] splits = field.split(":");
                    if (splits.length == 1) {
                        field = String.format("/0/%s", field);
                    } else {
                        field = String.format("/%s/%s", splits[0], splits[1]);
                    }
                }
                assert node != null;
                JsonNode jsonNode = node.at(field);
                String val = jsonNode.asText();
                if ("".equals(val)) {
                    val = jsonNode.toString();
                }
                matcher.appendReplacement(key, val);
            }
            matcher.appendTail(key);
            String subject = flowMonitor.subject();
            FlowCollectorFactory.doCollect(subject, key.toString());
        } catch (Exception e) {
            log.error("collect flow fail", e);
        }
    }
}
