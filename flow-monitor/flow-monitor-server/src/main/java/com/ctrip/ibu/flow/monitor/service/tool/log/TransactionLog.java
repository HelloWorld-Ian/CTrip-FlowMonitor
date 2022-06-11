package com.ctrip.ibu.flow.monitor.service.tool.log;

import com.dianping.cat.Cat;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * TransactionLog
 *
 * @author chenyijiang
 */
public class TransactionLog {

    private Map<String, String> innerIndexTags = new HashMap<>();

    private Map<String, String> innerStoreTags = new HashMap<>();

    private String scenario;

    private LogAction logAction;

    public TransactionLog(String scenario, LogAction logAction) {
        this.scenario = scenario;
        this.logAction = logAction;
    }

    public void send() {
        this.send("undefined");
    }

    public void send(String processAction) {
        this.sendTransaction(processAction);
        this.logIndexTag("processAction", processAction);
        this.sendEs();
    }

    public void sendEs() {
        if (MapUtils.isNotEmpty(innerIndexTags) || MapUtils.isNotEmpty(innerStoreTags)) {
            Cat.logTags(this.scenario, innerIndexTags, innerStoreTags);
        }
    }

    public void sendTransaction(String processActionName) {
        MetricLog.logTransaction(logAction.name(), processActionName, innerIndexTags, innerStoreTags);
    }

    public TransactionLog logIndexTag(String key, String value) {
        innerIndexTags.put(key, value);
        return this;
    }

    public TransactionLog logStoreTag(String key, String value) {
        innerStoreTags.put(key, value);
        return this;
    }

}
