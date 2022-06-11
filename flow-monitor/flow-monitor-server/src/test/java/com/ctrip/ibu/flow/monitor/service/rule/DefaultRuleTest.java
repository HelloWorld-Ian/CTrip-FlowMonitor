package com.ctrip.ibu.flow.monitor.service.rule;

import com.ctrip.ibu.flow.monitor.service.bo.FlowResult;
import com.ctrip.ibu.flow.monitor.service.constant.RuleJudgeStrategy;
import org.junit.Assert;
import org.junit.Test;

public class DefaultRuleTest {

    @Test
    public void testJudge() {
        FlowResult result = buildFlowResult();
        // MORE_THAN
        Rule rule1 = new DefaultRule("rule1", 750L, RuleJudgeStrategy.MORE_THAN.name());
        Assert.assertTrue(rule1.judge(result));
        // LESS_THAN
        Rule rule2 = new DefaultRule("rule2", 1500L, RuleJudgeStrategy.LESS_THAN.name());
        Assert.assertTrue(rule2.judge(result));
        // UP_THROUGH
        Rule rule3 = new DefaultRule("rule3", 750L, RuleJudgeStrategy.UP_THROUGH.name());
        Assert.assertTrue(rule3.judge(result));
        // DOWN_THROUGH
        result.setPreCount(2000L);
        Rule rule4 = new DefaultRule("rule4", 1500L, RuleJudgeStrategy.DOWN_THROUGH.name());
        Assert.assertTrue(rule4.judge(result));
    }

    public FlowResult buildFlowResult() {
        FlowResult flowResult = new FlowResult();
        flowResult.setEndTime(System.currentTimeMillis());
        flowResult.setStartTime(System.currentTimeMillis() - 100000L);
        flowResult.setKey("keyTest");
        flowResult.setSubject("subjectTest");
        flowResult.setCurCount(1000L);
        flowResult.setPreCount(500L);
        return flowResult;
    }
}