package com.ctrip.ibu.flow.monitor.collector.exception;

/**
 * @author Ian
 * @date 2022/6/3
 */
public class SubjectNotFoundException extends RuntimeException {
    public SubjectNotFoundException(String str, Object... args) {
        super(String.format(str, args));
    }
}
