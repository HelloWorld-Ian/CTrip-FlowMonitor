package com.ctrip.ibu.flow.monitor.collector.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubjectInfo {
    String appId;
    String name;
    String timeUnit;
    Long span;
    Integer splits;
}
