package com.ctrip.ibu.flow.monitor.service.qmq;

import com.ctrip.ibu.flow.monitor.service.bo.RuleHitInform;

import java.util.List;

/**
 * @author Ian
 * @date 2022/6/3
 */
public interface RuleHitInformProvider {
    void send(List<RuleHitInform> ruleHitInforms);
}
