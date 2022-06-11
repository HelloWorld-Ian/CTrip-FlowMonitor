package com.ctrip.ibu.flow.monitor.trigger.bo;

import lombok.*;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleHitInform {
    private String subject;
    private String key;
    private String appId;
    private String ruleName;
    private Long timestamp;
    private Long span;
    private Long count;
    private Long increment;
    private String strategy;
}
