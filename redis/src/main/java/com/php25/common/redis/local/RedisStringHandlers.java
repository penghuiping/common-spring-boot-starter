package com.php25.common.redis.local;

import com.php25.common.core.util.JsonUtil;
import org.springframework.data.util.Pair;


/**
 * @author penghuiping
 * @date 2021/3/3 09:15
 */
class RedisStringHandlers {

    final static Pair<String, RedisCmdHandler> STRING_GET = Pair.of(RedisCmd.STRING_GET, (redisManager, request, response) -> {
        LruCachePlus cache = redisManager.cache;
        String key = request.getParams().get(0).toString();
        ExpiredCache expiredCacheObject = cache.getValue(key);
        //没有此缓存
        if (expiredCacheObject == null) {
            response.setResult(null);
            return;
        }

        //缓存过期
        if (expiredCacheObject.isExpired()) {
            cache.remove(key);
            response.setResult(null);
            return;
        }
        response.setResult(expiredCacheObject);
    });

    final static Pair<String, RedisCmdHandler> STRING_SET = Pair.of(RedisCmd.STRING_SET, (redisManager, request, response) -> {
        LruCachePlus cache = redisManager.cache;
        String key = request.getParams().get(0).toString();
        Object value = request.getParams().get(1);
        Long expireTime = Constants.DEFAULT_EXPIRED_TIME;
        if (request.getParams().size() > 2) {
            expireTime = (Long) request.getParams().get(2);
        }
        ExpiredCache expiredCache = new ExpiredCache(expireTime, key, JsonUtil.toJson(value));
        cache.putValue(key, expiredCache);
        response.setResult(expiredCache);
    });

    final static Pair<String, RedisCmdHandler> STRING_SET_NX = Pair.of(RedisCmd.STRING_SET_NX, (redisManager, request, response) -> {
        LruCachePlus cache = redisManager.cache;
        String key = request.getParams().get(0).toString();
        Object value = request.getParams().get(1);
        Long expireTime = Constants.DEFAULT_EXPIRED_TIME;
        if (request.getParams().size() > 2) {
            expireTime = (Long) request.getParams().get(2);
        }
        ExpiredCache expiredCache = new ExpiredCache(expireTime, key, JsonUtil.toJson(value));
        cache.getValue(key);
        cache.putValueIfAbsent(key, expiredCache);
        cache.getValue(key);
        response.setResult(expiredCache);
    });

    final static Pair<String, RedisCmdHandler> STRING_INCR = Pair.of(RedisCmd.STRING_INCR, (redisManager, request, response) -> {
        LruCachePlus cache = redisManager.cache;
        String key = request.getParams().get(0).toString();
        ExpiredCache expiredCache = cache.getValue(key);
        if (null == expiredCache) {
            Long res = 1L;
            expiredCache = new ExpiredCache(Constants.DEFAULT_EXPIRED_TIME, key, JsonUtil.toJson(res));
            cache.putValue(key, expiredCache);
            response.setResult(expiredCache);
            return;
        }
        Long res = JsonUtil.fromJson(expiredCache.getValue().toString(), Long.class);
        res = res + 1;
        expiredCache.setValue(JsonUtil.toJson(res));
        cache.putValue(key, expiredCache);
        response.setResult(expiredCache);
    });

    final static Pair<String, RedisCmdHandler> STRING_DECR = Pair.of(RedisCmd.STRING_DECR, (redisManager, request, response) -> {
        LruCachePlus cache = redisManager.cache;
        String key = request.getParams().get(0).toString();
        ExpiredCache expiredCache = cache.getValue(key);
        if (null == expiredCache) {
            Long res = 1L;
            expiredCache = new ExpiredCache(Constants.DEFAULT_EXPIRED_TIME, key, JsonUtil.toJson(res));
            cache.putValue(key, expiredCache);
            response.setResult(expiredCache);
            return;
        }
        Long res = JsonUtil.fromJson(expiredCache.getValue().toString(), Long.class);
        res = res - 1;
        expiredCache.setValue(JsonUtil.toJson(res));
        cache.putValue(key, expiredCache);
        response.setResult(expiredCache);
    });

    final static Pair<String, RedisCmdHandler> REMOVE = Pair.of(RedisCmd.REMOVE, (redisManager, request, response) -> {

    });

    final static Pair<String, RedisCmdHandler> EXISTS = Pair.of(RedisCmd.EXISTS, (redisManager, request, response) -> {

    });

    final static Pair<String, RedisCmdHandler> GET_EXPIRE = Pair.of(RedisCmd.GET_EXPIRE, (redisManager, request, response) -> {

    });

    final static Pair<String, RedisCmdHandler> EXPIRE = Pair.of(RedisCmd.EXPIRE, (redisManager, request, response) -> {

    });

    final static Pair<String, RedisCmdHandler> EXPIRE_AT = Pair.of(RedisCmd.EXPIRE_AT, (redisManager, request, response) -> {

    });

}
