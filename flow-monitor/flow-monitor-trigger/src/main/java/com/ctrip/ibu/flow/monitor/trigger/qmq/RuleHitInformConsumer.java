package com.ctrip.ibu.flow.monitor.trigger.qmq;

import qunar.tc.qmq.Message;

/**
 * @author Ian
 * @date 2022/6/3
 */
public interface RuleHitInformConsumer {
    void handle(Message message);
}
