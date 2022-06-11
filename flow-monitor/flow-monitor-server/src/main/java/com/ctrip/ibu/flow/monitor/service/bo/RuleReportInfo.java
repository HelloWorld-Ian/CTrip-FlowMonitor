package com.ctrip.ibu.flow.monitor.service.bo;

import com.ctrip.ibu.flow.monitor.service.rule.DefaultRule;
import com.ctrip.ibu.flow.monitor.service.rule.Rule;
import lombok.*;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleReportInfo {
    private String name;
    private String strategy;
    private Long threshold;
    private Long interval;

    public Rule convert() {
        return new DefaultRule(name, threshold, strategy);
    }
}
