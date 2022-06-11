package com.ctrip.ibu.flow.monitor.service.bo;

import com.ctrip.ibu.flow.monitor.service.rule.Rule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlowRefresh {
    String key;
    Long count;
    Long timestamp;
    List<Rule> rules;

    SubjectInfo subjectInfo;
}
