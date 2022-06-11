package com.ctrip.ibu.flow.monitor.service.tool.redis;

import credis.java.client.CacheProvider;

/**
 * @author Ian
 * @date 2022/6/3
 */
public class CRedisClient {
    private static CacheProvider cacheProvider;

    public static void setCacheProvider(CacheProvider cacheProvider) {
        CRedisClient.cacheProvider = cacheProvider;
    }

    /**
     * 初始化redis client
     */
    public static CacheProvider getCacheProvider() {
        return cacheProvider;
    }
}
