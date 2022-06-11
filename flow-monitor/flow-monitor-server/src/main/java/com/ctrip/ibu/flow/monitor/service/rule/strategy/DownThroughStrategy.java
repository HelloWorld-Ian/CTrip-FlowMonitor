package com.ctrip.ibu.flow.monitor.service.rule.strategy;

import com.ctrip.ibu.flow.monitor.service.bo.FlowResult;

/**
 * 订阅策略：流量从大于阈值变成小于阈值触发规则
 *
 * @author Ian
 * @date 2022/6/3
 */
public class DownThroughStrategy implements Strategy {
    @Override
    public boolean judge(FlowResult result, long threshold) {
        return result.getCurCount() <= threshold && result.getPreCount() > threshold;
    }
}
