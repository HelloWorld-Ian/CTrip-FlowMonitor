package com.ctrip.ibu.flow.monitor.service.bo;

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
public class SubjectInfo {
    private String appId;
    private String name;
    private Long span;
    private Integer splits;
    private String timeUnit;

    public String subjectKey() {
        return String.format("%s-%s-%s-%s", appId,
                name, span, splits);
    }
}
