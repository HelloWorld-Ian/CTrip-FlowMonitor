package com.ctrip.ibu.flow.monitor.trigger.event;

import com.ctrip.ibu.flow.monitor.trigger.bo.FlowInfo;

import java.util.List;

/**
 * @author Ian
 * @date 2022/6/3
 */
public interface Event {
    void doEvent(List<FlowInfo> flowInfo);
}
