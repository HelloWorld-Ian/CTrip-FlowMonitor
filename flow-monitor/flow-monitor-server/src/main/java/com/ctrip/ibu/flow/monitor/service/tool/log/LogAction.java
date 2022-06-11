package com.ctrip.ibu.flow.monitor.service.tool.log;

/**
 * Log actions, use to disable some logs.
 * @author chenyijiang
 */
public enum LogAction {
    // self soa
    SELF_SOA,
    // upstream soa
    UPSTREAM_SOA,
    // cache query
    CACHE_QUERY,
    // cache update
    CACHE_UPDATE,
    // delete
    CACHE_DELETE,
    // the sketch of cache
    CACHE_SKETCH,
    // result of different stages during the whole request.
    PROCESS,
    // produce qmq message
    QMQ_PRODUCE,
    // send qmq message
    QMQ_SEND,
    // consume qmq message
    QMQ_RECEIVE,
    // consume qmq message
    QMQ_CONSUME
}