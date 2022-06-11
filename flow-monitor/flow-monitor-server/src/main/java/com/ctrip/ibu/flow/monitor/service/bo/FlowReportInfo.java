package com.ctrip.ibu.flow.monitor.service.bo;

import lombok.*;

import java.util.List;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlowReportInfo {
    String key;
    String type;
    Long count;
    Long timestamp;
    List<RuleReportInfo> rules;
    SubjectInfo subjectInfo;
}
