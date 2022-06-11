package com.ctrip.ibu.flow.monitor.service.subject.factory;


import com.ctrip.ibu.flow.monitor.service.bo.SubjectInfo;
import com.ctrip.ibu.flow.monitor.service.subject.Subject;

/**
 * @author Ian
 * @date 2022/6/3
 */
public interface Factory {
    Subject get(SubjectInfo info);
}
