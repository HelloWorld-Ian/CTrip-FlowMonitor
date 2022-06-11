package com.ctrip.ibu.flow.monitor.service.tool.log;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * @author ling.zheng
 * @date 2019/7/11 19:59
 */
@Slf4j
public class MetricLog {

    /**
     * ES scenario name
     */
    public static final String SCENARIO = "ibu-market-flow-monitor";

    /**
     * trace ids thread local initialized
     */
    private static final InheritableThreadLocal<TraceIdentity> TRACE_IDENTITY = new InheritableThreadLocal<>();

    /**
     * index tag thread local initialized
     */
    private static final ThreadLocal<Map<String, String>> INDEX_TAG = ThreadLocal.withInitial(HashMap::new);

    /**
     * store tag thread local initialized
     */
    private static final ThreadLocal<Map<String, String>> STORE_TAG = ThreadLocal.withInitial(HashMap::new);

    /**
     * Create instant log entity of specified log action
     * and default parent cat transaction id, which was initialized when the trace was started.
     *
     * @param logType log action
     * @return instant log entity
     */
    public static TransactionLog createInstant(@NonNull LogAction logType) {
        return createInstant(logType, null);
    }

    /**
     * Create instant log entity of specified log action and parent cat transaction id.
     *
     * @param logType       log action
     * @param traceIdentity traceIdentity
     * @return instant log entity
     */
    public static TransactionLog createInstant(@NonNull LogAction logType, TraceIdentity traceIdentity) {
        TransactionLog log = new TransactionLog(SCENARIO, logType);
        log.logIndexTag(MetricIndex.logType.name(), String.valueOf(logType));
        if (traceIdentity != null) {
            log.logIndexTag(MetricIndex.traceId.name(), String.valueOf(traceIdentity.traceId));
            log.logIndexTag(MetricIndex.messageId.name(), traceIdentity.getChildTransactionId(logType));
            log.logIndexTag(MetricIndex.parentCatTransaction.name(), traceIdentity.parentTransactionId);
            log.logIndexTag(MetricIndex.rootCatTransaction.name(), traceIdentity.rootTransactionId);
        }
        return log;
    }

    public static void recordLogs(Map<String, String> map) {
        Cat.logTags(SCENARIO, map, null);
    }

    /**
     * Start a trace with specific trace id.
     * And the current cat transaction message id will be stored as the parent message id
     * for all the instant logs created with {@link MetricLog#createInstant},
     * so that all the transactions can be associated in one transaction log.
     *
     * @param traceId user defined trace id, stored as an index.
     */
    public static void startTrace(long traceId) {
        // if the 'get' method is called before set method, the traceId from parent thread will be accessible.
        if (TRACE_IDENTITY.get() == null) {
            MessageTree messageTree = getMessageTree();
            String current = Cat.getCurrentMessageId();
            String root = StringUtils.defaultString(messageTree.getRootMessageId(), current);
            TraceIdentity identity = new TraceIdentity(String.valueOf(traceId), current, root);
            TRACE_IDENTITY.set(identity);
            putIndex(MetricIndex.traceId, String.valueOf(identity.traceId));
            putIndex(MetricIndex.messageId, identity.parentTransactionId);
            putIndex(MetricIndex.rootCatTransaction, identity.rootTransactionId);
        }
    }

    /**
     * End current trace. Delete the trace id and parent cat transaction id.
     */
    public static void endTrace() {
        TRACE_IDENTITY.remove();
    }

    /**
     * @return parent cat transaction id, null if no trace was started.
     */
    public static Optional<TraceIdentity> getTraceIdentity() {
        return Optional.ofNullable(TRACE_IDENTITY.get());
    }

    public static String createChildMessageId() {
        String mid = Cat.createMessageId();
        Cat.logEvent(CatConstants.TYPE_REMOTE_CALL, CatConstants.NAME_REQUEST, Event.SUCCESS, mid);
        return mid;
    }

    /**
     * put index tag
     *
     * @param metricIndex index tag name
     * @param value       index tag value
     */
    public static void putIndex(MetricIndex metricIndex, String value) {
        INDEX_TAG.get().put(metricIndex.name(), value);
    }

    /**
     *
     * @param metricIndex index tag name
     * @return index tag value if present
     */
    public static String getIndexValue(MetricIndex metricIndex) {
        return INDEX_TAG.get().get(metricIndex.name());
    }

    /**
     * put store message
     *
     * @param metricStore store name
     * @param value       store value
     */
    public static void putStore(MetricStore metricStore, String value) {
        STORE_TAG.get().put(metricStore.name(), value);
    }

    /**
     * put exception message
     *
     * @param e exception
     */
    public static void putException(Throwable e) {
        INDEX_TAG.get().put(MetricIndex.exceptionType.name(), e.getClass().getCanonicalName());
        STORE_TAG.get().put(MetricStore.exception.name(), buildExceptionStack(e));
    }

    /**
     * write log to ES
     */
    public static void logTags() {
        logTags(false);
    }

    /**
     * write log to ES
     */
    public static void logTags(boolean endTrace) {
        Cat.logTags(SCENARIO, INDEX_TAG.get(), STORE_TAG.get());
        logTransaction(LogAction.SELF_SOA.name(), INDEX_TAG.get().getOrDefault(MetricIndex.methodName.name(), "unknown"),
                INDEX_TAG.get(), STORE_TAG.get());

        INDEX_TAG.remove();
        STORE_TAG.remove();
        if (endTrace) {
            endTrace();
        }
    }

    /**
     * write log to cat transaction.
     */
    static void logTransaction(String firstName, String secondName, Map<String, String> innerIndexTags,
                               Map<String, String> innerStoreTags) {
        Transaction transaction = Cat.newTransaction(firstName, secondName);
        appendAndSend(transaction, innerIndexTags, innerStoreTags);
    }

    /**
     * write log to cat transaction.
     */
    static void logForkedTransaction(String firstName, String secondName, Map<String, String> innerIndexTags,
                               Map<String, String> innerStoreTags) {
        ForkedTransaction transaction = Cat.newForkedTransaction(firstName, secondName);
        transaction.fork();
        appendAndSend(transaction, innerIndexTags, innerStoreTags);
    }

    private static void appendAndSend(Transaction transaction, Map<String, String> innerIndexTags,
                        Map<String, String> innerStoreTags) {

        addMessageIntoTransactionTree(innerIndexTags);
        innerIndexTags.forEach(transaction::addData);
        innerStoreTags.forEach(transaction::addData);
        transaction.setStatus(com.dianping.cat.message.Message.SUCCESS);
        transaction.complete();
    }

    /**
     * Add this message to the message tree if the transaction id was specified in the index tags.
     *
     * @param innerIndexTags index tags.
     */
    private static void addMessageIntoTransactionTree(Map<String, String> innerIndexTags) {
        if (MapUtils.isNotEmpty(innerIndexTags)) {
            String root = innerIndexTags.get(MetricIndex.rootCatTransaction.name());
            String parent = innerIndexTags.get(MetricIndex.parentCatTransaction.name());
            String current = innerIndexTags.get(MetricIndex.messageId.name());
            if (current != null || parent != null || root != null) {
                MessageTree messageTree = getMessageTree();
                messageTree.setRootMessageId(root);
                messageTree.setParentMessageId(parent);
                messageTree.setMessageId(current);
            }
        }
    }

    private static MessageTree getMessageTree() {
        MessageTree messageTree = Cat.getManager().getThreadLocalMessageTree();
        if (messageTree == null) {
            Cat.setup(null);
            messageTree = Cat.getManager().getThreadLocalMessageTree();
        }
        return messageTree;
    }

    public static String buildExceptionStack(Throwable throwable) {
        if (throwable != null) {
            StringWriter writer = new StringWriter(2048);
            throwable.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        } else {
            return StringUtils.EMPTY;
        }
    }

    public enum MetricIndex {
        logType,
        exceptionType,
        methodName,
        traceId,
        rootCatTransaction,
        parentCatTransaction,
        messageId,
        /**
         * 规则命中的subject appId
         */
        appId,
        /**
         * subject name
         */
        subjectName,
        /**
         * rule name
         */
        ruleName,
        /**
         * 规则命中的key值
         */
        key,
        /**
         * 流量大小
         */
        count,
        /**
         * 时间
         */
        timestamp,
        /**
         * 统计长度
         */
        span
        ;
    }

    public enum MetricStore {
        exception,
        data,
        ;
    }

    public static class TraceIdentity {

        private final String traceId;

        private Map<LogAction, String> childTransactionIds;

        private final String parentTransactionId;

        private final String rootTransactionId;

        public TraceIdentity(String traceId, String parentTransactionId, String rootTransactionId) {
            this.traceId = traceId;
            this.parentTransactionId = parentTransactionId;
            this.rootTransactionId = rootTransactionId;
        }

        public String getTraceId() {
            return traceId;
        }

        public String getParentTransactionId() {
            return parentTransactionId;
        }

        public void addChildTransactionId(LogAction logAction, String messageId) {
            if (childTransactionIds == null) {
                childTransactionIds = new HashMap<>();
            }
            childTransactionIds.put(logAction, messageId);
        }

        public String getChildTransactionId(LogAction l) {
            return childTransactionIds == null ? null : childTransactionIds.get(l);
        }

        public String getRootTransactionId() {
            return rootTransactionId;
        }
    }

}
