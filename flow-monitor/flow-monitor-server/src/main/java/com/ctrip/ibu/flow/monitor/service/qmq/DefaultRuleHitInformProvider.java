package com.ctrip.ibu.flow.monitor.service.qmq;

import com.ctrip.ibu.flow.monitor.service.bo.RuleHitInform;
import com.ctrip.ibu.flow.monitor.service.tool.json.JsonUtils;
import com.ctrip.ibu.flow.monitor.service.tool.log.LogAction;
import com.ctrip.ibu.flow.monitor.service.tool.log.MetricLog;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import qunar.tc.qmq.Message;
import qunar.tc.qmq.producer.MessageProducerProvider;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Slf4j
@Component
@SuppressWarnings("all")
public class DefaultRuleHitInformProvider implements RuleHitInformProvider {

    MessageProducerProvider producer = new MessageProducerProvider();
    private final static String SUBJECT =  "ibu.market.flow.trigger";
    /**
     * 规则命中通知过长时分批发送
     */
    private final Integer MAX_MERGE = 100;

    @PostConstruct
    private void init() {
        producer.init();
    }

    /**
     * 发送规则命中通知
     */
    @Override
    public void send(List<RuleHitInform> ruleHitInforms) {
        if (Objects.isNull(ruleHitInforms) || ruleHitInforms.isEmpty()) {
            return;
        }
        List<List<RuleHitInform>> splits = Lists.partition(ruleHitInforms, MAX_MERGE);
        for (List<RuleHitInform> split : splits) {
            Message message = buildMsg(split);
            producer.sendMessage(message);
        }
    }

    private Message buildMsg(List<RuleHitInform> ruleHitInform) {
        RuleHitInform represent = ruleHitInform.get(0);
        Message msg = producer.generateMessage(SUBJECT);
        String data = JsonUtils.toJson(ruleHitInform);
        msg.setProperty("data", data);
        return msg;
    }
}
