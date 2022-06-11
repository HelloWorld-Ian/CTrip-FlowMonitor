package com.ctrip.ibu.flow.monitor.trigger.qmq;

import com.ctrip.framework.foundation.Foundation;
import com.ctrip.ibu.flow.monitor.trigger.bo.RuleHitInform;
import qunar.tc.qmq.Message;
import qunar.tc.qmq.consumer.MessageConsumerProvider;
import com.ctrip.ibu.flow.monitor.trigger.service.EventService;
import com.ctrip.ibu.flow.monitor.trigger.tool.JsonUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author Ian
 * @date 2022/6/3
 */
public class DefaultRuleHitInformConsumer implements RuleHitInformConsumer {

    private final MessageConsumerProvider messageConsumer = new MessageConsumerProvider();
    private final EventService eventService = EventService.getInstance();
    private final static DefaultRuleHitInformConsumer defaultRuleHitInformConsumer = new DefaultRuleHitInformConsumer();

    private final static String SUBJECT = "ibu.market.flow.trigger";

    private DefaultRuleHitInformConsumer() {
        init();
    }
    public static RuleHitInformConsumer getInstance() {
        return defaultRuleHitInformConsumer;
    }

    public void init() {
        messageConsumer.init();
        messageConsumer.addListener(SUBJECT, String.format("%s-flow-monitor-server",
                Foundation.app().getAppId()), this::handle);
    }

    @Override
    public void handle(Message msg) {
        List<RuleHitInform> ruleHitInform = convert(msg);
        if (!Objects.isNull(ruleHitInform)) {
            eventService.invoke(ruleHitInform);
        }
    }

    private List<RuleHitInform> convert(Message message) {
        String data = message.getStringProperty("data");
        if (Objects.isNull(data)) {
            return null;
        }
        return JsonUtils.readValues(data, RuleHitInform.class);
    }
}
