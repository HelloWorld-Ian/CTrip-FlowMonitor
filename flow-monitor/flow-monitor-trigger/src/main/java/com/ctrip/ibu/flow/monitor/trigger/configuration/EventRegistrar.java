package com.ctrip.ibu.flow.monitor.trigger.configuration;

import com.ctrip.framework.foundation.Foundation;
import com.ctrip.ibu.flow.monitor.trigger.annotations.FlowWatchEvent;
import com.ctrip.ibu.flow.monitor.trigger.context.Trigger;
import com.ctrip.ibu.flow.monitor.trigger.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class EventRegistrar implements InitializingBean, ApplicationContextAware {

    private  ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> map = applicationContext.getBeansWithAnnotation(FlowWatchEvent.class);
        for (Object v : map.values()) {
            FlowWatchEvent flowWatchEvent = v.getClass().getAnnotation(FlowWatchEvent.class);
            String[] watch = flowWatchEvent.watch();
            for (String s : watch) {
                register((Event) v, s);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * @param watch appId-subject-rule or subject-rule
     */
    public void register(Event event, String watch) {
        if (!Objects.isNull(watch)) {
            String[] combine = watch.split("-");
            String appId;
            String subjectName;
            String rule;
            if (combine.length == 3) {
                appId = combine[0];
                subjectName = combine[1];
                rule = combine[2];
            } else if (combine.length == 2) {
                appId = Foundation.app().getAppId();
                subjectName = combine[0];
                rule = combine[1];
            } else {
                return;
            }
            Trigger.watch(String.format("%s-%s-%s", appId, subjectName, rule), event);
        }
    }
}
