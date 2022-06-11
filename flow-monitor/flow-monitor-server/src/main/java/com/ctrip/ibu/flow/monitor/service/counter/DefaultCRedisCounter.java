package com.ctrip.ibu.flow.monitor.service.counter;

import com.ctrip.ibu.flow.monitor.service.bo.FlowResult;
import com.ctrip.ibu.flow.monitor.service.tool.log.LogAction;
import com.ctrip.ibu.flow.monitor.service.tool.log.MetricLog;
import com.ctrip.ibu.flow.monitor.service.tool.redis.CRedisClient;
import credis.java.client.CacheProvider;
import credis.java.client.pipeline.CachePipeline;
import credis.java.client.transaction.RedisCASAction;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * 分key式滑动窗口
 *
 * @author Ian
 * @date 2022/6/3
 */
@Slf4j
public class DefaultCRedisCounter implements Counter {
    // appid-subject
    private final String subject;
    private final String key;
    private final long span;
    private final long splits;
    private final long quantum;

    private final String[] KEYS;
    private final String LOCK;

    private static final ThreadLocal<String> randomId = new ThreadLocal<>();
    private static final ThreadLocal<Object> locked = new ThreadLocal<>();


    public DefaultCRedisCounter(String subject, String key, long span, int splits) {
        this.subject = subject;
        this.key = key;
        this.span = span;
        this.splits = splits;
        this.quantum = this.span / this.splits;
        String counterKey = String.format("monitor-%s-%s-window", subject, key);
        LOCK = String.format("%s-lock", counterKey);

        KEYS = new String[splits];
        for (int i = 0; i < splits; i++) {
            KEYS[i] = String.format("%s-%s", counterKey, i);
        }
    }

    @Override
    public FlowResult current() {
        long cur = count();
        return buildRes(cur, cur);
    }

    @Override
    public FlowResult incr(long count, long timestamp) {
        try {
            // 分布式加锁
            lock();
            // 丢弃过期的流量记录
            if (timestamp >= System.currentTimeMillis() - span) {
                CacheProvider cacheProvider = CRedisClient.getCacheProvider();
                CachePipeline pipeline = cacheProvider.getPipeline();
                ShardInfo shardInfo = shard(timestamp);

                long ttl = cacheProvider.ttl(KEYS[shardInfo.getIndex()]);
                pipeline.incrBy(KEYS[shardInfo.getIndex()], count);
                if (ttl < 0) {
                    // unix timestamp 单位是秒！！
                    pipeline.expire(KEYS[shardInfo.getIndex()], (int) (shardInfo.getEndTime() / 1000));
                } else if (ttl == 0) {
                    // 临界位置防止误过期
                    incr(count, timestamp);
                }
                pipeline.sync();
            } else {
                count = 0;
            }
            long cur = count();
            return buildRes(cur, cur - count);
        } finally {
            unlock();
        }
    }

    /**
     * 获取当前流量所属分片
     */
    private ShardInfo shard(long timestamp) {
        int key = (int) ((timestamp / quantum) % splits);
        long expire = span - timestamp % quantum;
        return ShardInfo.builder()
                .index(key)
                .endTime(expire).build();
    }

    /**
     * 计算总流量
     */
    private long count() {
        CacheProvider provider = CRedisClient.getCacheProvider();
        List<String> ret = provider.mget(KEYS);
        long count = 0;
        for (String s : ret) {
            if (!Objects.isNull(s)) {
                count += Long.parseLong(s);
            }
        }

        String[] appIdAndName = subject.split("-");
        MetricLog.createInstant(LogAction.PROCESS)
                .logIndexTag(MetricLog.MetricIndex.appId.name(), appIdAndName[0])
                .logIndexTag(MetricLog.MetricIndex.subjectName.name(), appIdAndName[1])
                .logIndexTag(MetricLog.MetricIndex.count.name(), Long.toString(count))
                .logIndexTag(MetricLog.MetricIndex.span.name(), Long.toString(span))
                .logIndexTag(MetricLog.MetricIndex.timestamp.name(), Long.toString(System.currentTimeMillis()))
                .send("FlowInfo");

        return count;
    }

    /**
     * 构造结果
     */
    private FlowResult buildRes(long cur, long pre) {
        long curTime = System.currentTimeMillis();
        return new FlowResult(cur, pre, curTime - span, curTime, key, subject);
    }

    /**
     * 分布式锁
     */
    private void lock() {
        String id = UUID.randomUUID().toString();
        randomId.set(id);
        try {
            tryLock(id, 65);
        } catch (Exception e) {
            log.error("lock fail", e);
        }
    }

    private void tryLock(String val, int repeatTimes) throws Exception {
        CacheProvider cacheProvider = CRedisClient.getCacheProvider();
        if (!Objects.isNull(locked.get())) {
            return;
        }
        if (!cacheProvider.set(LOCK, val, "NX", "EX", 30)) {
            if (repeatTimes > 0) {
                Thread.sleep(500);
                tryLock(val, repeatTimes - 1);
            } else {
                // 检查异常情况
                long ttl = cacheProvider.ttl(LOCK);
                if (ttl > 30 || ttl == -1) {
                    cacheProvider.del(LOCK);
                }
                throw new TimeoutException("try lock time out");
            }
        } else{
            locked.set(true);
        }
    }

    /**
     * 解锁操作
     */
    private void unlock()  {
        CacheProvider cacheProvider = CRedisClient.getCacheProvider();
        String expected = randomId.get();
        String actual = cacheProvider.get(LOCK);
        // 确认unlock线程的身份
        if (Objects.equals(expected, actual)) {
            cacheProvider.del(LOCK);
        }
        locked.remove();
    }


    @Getter
    @Builder
    private static class ShardInfo {
        private int index;
        private long endTime;
    }
}
