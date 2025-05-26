package com.ktt.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class AirPriceCacheStore{
//    private final Map<String, String> cache = new ConcurrentHashMap<>();
//
//    public void store(String sessionId, String airPriceResponseXml) {
//        cache.put(sessionId, airPriceResponseXml);
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

    public void store(String sessionId, String airPriceXml) {
        redisTemplate.opsForValue().set(sessionId, airPriceXml, TTL_MINUTES, TimeUnit.MINUTES);
    }

    public String get(String sessionId) {
        return redisTemplate.opsForValue().get(sessionId);
    }

    public void remove(String sessionId) {
        redisTemplate.delete(sessionId);
    }

}