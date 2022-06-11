package com.ctrip.ibu.flow.monitor.service.counter;


import com.ctrip.ibu.flow.monitor.service.bo.FlowResult;

/**
 * @author Ian
 * @date 2022/6/3
 */
public interface Counter {
    /**
     * 流量采样，返回当前流量
     */
    FlowResult current();

    /**
     * 流量增加
     * @param count 流量增加量
     * @param timestamp 流量增加时间
     * @return 返回当前流量
     */
    FlowResult incr(long count, long timestamp);
}
