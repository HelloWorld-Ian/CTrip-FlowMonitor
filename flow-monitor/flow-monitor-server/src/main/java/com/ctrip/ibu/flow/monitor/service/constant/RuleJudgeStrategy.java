package com.ctrip.ibu.flow.monitor.service.constant;

import com.ctrip.ibu.flow.monitor.service.rule.strategy.*;

/**
 * @author Ian
 * @date 2022/6/3
 */
public enum RuleJudgeStrategy {
    MORE_THAN(new MoreThanStrategy()),
    LESS_THAN(new LessThanStrategy()),
    UP_THROUGH(new UpThroughStrategy()),
    DOWN_THROUGH(new DownThroughStrategy());

    public Strategy strategy;
    RuleJudgeStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
}
