package com.ctrip.ibu.flow.monitor.trigger.service;

import com.ctrip.ibu.flow.monitor.trigger.bo.RuleHitInform;
import com.ctrip.ibu.flow.monitor.trigger.event.Event;
import com.ctrip.ibu.flow.monitor.trigger.bo.FlowInfo;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Ian
 * @date 2022/6/3
 */
public class EventService {
    private final Map<String, List<Event>> eventWatchMap = new ConcurrentHashMap<>();
    private final static EventService eventService = new EventService();
    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(2,
            4,
            5000,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1000));

    private EventService(){}
    public static EventService getInstance() {
        return eventService;
    }


    /**
     * 执行响应
     */
    public void invoke(List<RuleHitInform> ruleHitInform) {
        if (Objects.isNull(ruleHitInform) || ruleHitInform.isEmpty()) {
            return;
        }
        List<List<RuleHitInform>> groups = groupByRuleName(ruleHitInform);
        for (List<RuleHitInform> group : groups) {
            List<FlowInfo> flowInfo = group.stream()
                    .map(this::buildFlowInfo).collect(Collectors.toList());
            // group为同一subject、rule下
            List<Event> events = eventWatchMap.get(buildKey(group.get(0)));
            if (!Objects.isNull(events)) {
                poolExecutor.execute(() -> {
                    for (Event event : events) {
                        event.doEvent(flowInfo);
                    }
                });
            }
        }
    }

    public List<List<RuleHitInform>> groupByRuleName(List<RuleHitInform> ruleHitInform) {
        Map<String, List<RuleHitInform>> map = new HashMap<>();
        for (RuleHitInform hitInform : ruleHitInform) {
            List<RuleHitInform> informs = map.computeIfAbsent(hitInform.getRuleName(), s -> new ArrayList<>());
            informs.add(hitInform);
        }
        return new ArrayList<>(map.values());
    }

    /**
     * 事件注册
     * @param key appId-subjectName-ruleName
     * @param event 自定义响应事件
     */
    public void watch(String key, Event event) {
        List<Event> events = eventWatchMap.computeIfAbsent(key, s -> new ArrayList<>());
        events.add(event);
    }

    private String buildKey(RuleHitInform ruleHitInform) {
        return String.format("%s-%s-%s", ruleHitInform.getAppId(),
                ruleHitInform.getSubject(), ruleHitInform.getRuleName());
    }

    private FlowInfo buildFlowInfo(RuleHitInform ruleHitInform) {
        return FlowInfo.builder()
                .count(ruleHitInform.getCount())
                .span(ruleHitInform.getSpan())
                .timestamp(ruleHitInform.getTimestamp())
                .key(ruleHitInform.getKey())
                .increment(ruleHitInform.getIncrement())
                .watch(String.format("%s-%s-%s", ruleHitInform.getAppId(),
                        ruleHitInform.getSubject(), ruleHitInform.getRuleName())).build();
    }
}
