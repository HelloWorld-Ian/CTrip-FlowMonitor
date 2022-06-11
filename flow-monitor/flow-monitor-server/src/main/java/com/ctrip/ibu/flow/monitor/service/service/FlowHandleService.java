package com.ctrip.ibu.flow.monitor.service.service;

import com.ctrip.ibu.flow.monitor.service.bo.FlowRefresh;
import com.ctrip.ibu.flow.monitor.service.bo.FlowSample;
import com.ctrip.ibu.flow.monitor.service.bo.RuleHitInform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ctrip.ibu.flow.monitor.service.qmq.RuleHitInformProvider;
import com.ctrip.ibu.flow.monitor.service.subject.Subject;
import com.ctrip.ibu.flow.monitor.service.subject.factory.Factory;

import java.util.List;
import java.util.Objects;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Service
public class FlowHandleService {

    @Autowired
    private Factory subjectFactory;

    @Autowired
    private RuleHitInformProvider ruleHitInformProvider;

    /**
     * 流量更新 + 实时订阅
     */
    public void doRefresh(FlowRefresh info) {
        if (Objects.isNull(info)) {
            return;
        }
        Subject subject = subjectFactory.get(info.getSubjectInfo());
        List<RuleHitInform> ret = subject.refresh(info);
        ruleHitInformProvider.send(ret);
    }

    /**
     * 流量采样 + 定时订阅
     */
    public void doSample(FlowSample info) {
        if (Objects.isNull(info)) {
            return;
        }
        Subject subject = subjectFactory.get(info.getSubjectInfo());
        // 需要避免重复触发
        if (subject.checkScheduledRule(info.getRule(), info.getInterval())) {
            List<RuleHitInform> ret = subject.sample(info);
            ruleHitInformProvider.send(ret);
        }
    }
}
