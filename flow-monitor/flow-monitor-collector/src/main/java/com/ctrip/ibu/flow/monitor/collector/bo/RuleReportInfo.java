package com.ctrip.ibu.flow.monitor.collector.bo;

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
public class RuleReportInfo {
    private String name;
    private String strategy;
    private Long threshold;
    private Long interval;
}
