package com.ctrip.ibu.flow.monitor.trigger.context;

import com.ctrip.ibu.flow.monitor.trigger.event.Event;
import com.ctrip.ibu.flow.monitor.trigger.qmq.DefaultRuleHitInformConsumer;
import com.ctrip.ibu.flow.monitor.trigger.qmq.RuleHitInformConsumer;
import com.ctrip.ibu.flow.monitor.trigger.service.EventService;

/**
 * @author Ian
 * @date 2022/6/3
 */
public class Trigger {
    private final static RuleHitInformConsumer consumer = DefaultRuleHitInformConsumer.getInstance();
    private final static EventService eventService = EventService.getInstance();

    /**
     * 事件注册
     * @param key appId-subjectName-ruleName
     * @param event 自定义响应事件
     */
    public static void watch(String key, Event event) {
        eventService.watch(key, event);
    }
}
