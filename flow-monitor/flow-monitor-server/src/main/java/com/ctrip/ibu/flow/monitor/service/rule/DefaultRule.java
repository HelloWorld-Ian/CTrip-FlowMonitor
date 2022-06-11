package com.ctrip.ibu.flow.monitor.service.rule;

import com.ctrip.ibu.flow.monitor.service.bo.FlowResult;
import com.ctrip.ibu.flow.monitor.service.constant.RuleJudgeStrategy;
import com.ctrip.ibu.flow.monitor.service.rule.strategy.Strategy;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Getter
@Setter
public class DefaultRule implements Rule {
    private String name;
    private String strategyName;
    private Strategy strategy;
    private long threshold;

    public DefaultRule(String name, long threshold, String strategy) {
        this.name = name;
        this.threshold = threshold;
        RuleJudgeStrategy ruleJudgeStrategy = RuleJudgeStrategy.valueOf(strategy);
        this.strategy = ruleJudgeStrategy.strategy;
        this.strategyName = ruleJudgeStrategy.name();
    }

    @Override
    public boolean judge(FlowResult result) {
        return strategy.judge(result, threshold);
    }

}
