package com.ktt.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class AirLowFairCacheStore
{
//    private final Map<String, String> cache = new ConcurrentHashMap<>();
//    public void store(String sessionId, String lowFare) {
//        cache.put(sessionId, lowFare);
//    }
//
//    public String get(String sessionId) {
//        return cache.get(sessionId);
//    }
//
//    public void remove(String sessionId) {
//        cache.remove(sessionId);
//    }

    private static final long TTL_MINUTES = 30;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void store(String sessionId, String lowFareXml) {
        redisTemplate.opsForValue().set("lowfare:" + sessionId, lowFareXml, TTL_MINUTES, TimeUnit.MINUTES);
    }

    public String get(String sessionId) {
        return redisTemplate.opsForValue().get("lowfare:" + sessionId);
    }

    public void remove(String sessionId) {
        redisTemplate.delete("lowfare:" + sessionId);
    }
}
