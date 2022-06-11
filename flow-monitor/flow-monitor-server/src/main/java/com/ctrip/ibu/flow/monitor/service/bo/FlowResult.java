package com.ctrip.ibu.flow.monitor.service.bo;

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
public class FlowResult {
    private Long curCount;
    private Long preCount;
    private Long startTime;
    private Long endTime;

    private String key;
    private String subject;
}
