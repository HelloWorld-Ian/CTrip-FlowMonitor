package com.ctrip.ibu.flow.monitor.trigger.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlowInfo {
    /**
     * 流量计数
     */
    private Long count;
    /**
     * 流量统计时间长度
     */
    private Long span;
    /**
     * 事件响应触发时间
     */
    private Long timestamp;
    /**
     * 流量增量
     */
    private Long increment;
    /**
     * 流量对应的key
     */
    private String key;
    /**
     * appId-subjectName-ruleName
     */
    private String watch;
}
