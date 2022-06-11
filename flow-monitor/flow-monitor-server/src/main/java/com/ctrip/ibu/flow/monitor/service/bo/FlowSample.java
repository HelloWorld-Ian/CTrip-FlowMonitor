package com.ctrip.ibu.flow.monitor.service.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import com.ctrip.ibu.flow.monitor.service.rule.Rule;
import lombok.NoArgsConstructor;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlowSample {
    Rule rule;
    Long interval;

    SubjectInfo subjectInfo;
}
