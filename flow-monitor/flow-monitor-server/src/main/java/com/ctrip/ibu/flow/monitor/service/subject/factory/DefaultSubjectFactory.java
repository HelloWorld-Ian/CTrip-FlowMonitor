package com.ctrip.ibu.flow.monitor.service.subject.factory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.ctrip.ibu.flow.monitor.service.bo.SubjectInfo;
import com.ctrip.ibu.flow.monitor.service.subject.DefaultSubject;
import com.ctrip.ibu.flow.monitor.service.subject.Subject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Ian
 * @date 2022/6/3
 */
@Slf4j
@Component
public class DefaultSubjectFactory implements Factory  {

    /**
     * key : appId-subject-span-splits
     * subject 缓存
     */
    LoadingCache<String, Subject> subjectCache = CacheBuilder.newBuilder()
            .expireAfterAccess(6, TimeUnit.HOURS)
            .build(new CacheLoader<String, Subject>() {
                @Override
                public Subject load(String key) {
                    return initSubject(key);
                }
            });

    @Override
    public Subject get(SubjectInfo info) {
        String subjectKey = info.subjectKey();
        try {
            return subjectCache.get(subjectKey);
        } catch (ExecutionException e) {
            log.info("get subject fail", e);
        }
        return null;
    }

    public Subject initSubject(String key) {
        // key : appId-subject-span-splits
        String[] info = key.split("-");
        return new DefaultSubject(info[0], info[1], Long.parseLong(info[2]), Integer.parseInt(info[3]));
    }
}
