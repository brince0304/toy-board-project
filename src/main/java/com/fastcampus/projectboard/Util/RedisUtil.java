package com.fastcampus.projectboard.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class RedisUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private final String REDIS_KEY_PREFIX = "LOGOUT_";

    private final String EXPIRED_DURATION = "EXPIRE_DURATION";

    public boolean isFirstIpRequest(String clientAddress, Long articleId) {
        String key = generateViewKey(clientAddress, articleId);
        return !Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void writeClientRequest(String clientAddress, Long articleId) {
        String key = generateViewKey(clientAddress, articleId);
        stringRedisTemplate.opsForValue().set(key, "id"+articleId);
        stringRedisTemplate.expire(key, 60*60*24 , TimeUnit.SECONDS);
    }
    public boolean isFirstIpRequest2(String clientAddress, Long articleId) {
        String key = generateLikeKey(clientAddress, articleId);
        return !Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void writeClientRequest2(String clientAddress, Long articleId) {
        String key = generateLikeKey(clientAddress, articleId);
        stringRedisTemplate.opsForValue().set(key, "id"+articleId);
        stringRedisTemplate.expire(key, 60*60*24 , TimeUnit.SECONDS);
    }

    // key 형식 : 'client Address + postId' ->  '\xac\xed\x00\x05t\x00\x0f127.0.0.1 + 500'
    private String generateViewKey(String clientAddress, Long articleId) {
        return clientAddress + "view + " + articleId;
    }

    private String generateLikeKey(String clientAddress, Long articleId) {
        return clientAddress + "like + " + articleId;
    }



    public String getData(String key){
        ValueOperations<String,String> valueOperations = stringRedisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public void setData(String key, String value){
        ValueOperations<String,String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(key,value);
    }

    public String getDataByValue(String value){
        return Objects.requireNonNull(stringRedisTemplate.keys(value)).stream().findFirst().orElse(null);
    }

    public void setDataExpire(String key,String value,long duration){
        ValueOperations<String,String> valueOperations = stringRedisTemplate.opsForValue();
        Duration expireDuration = Duration.ofMillis(duration);
        valueOperations.set(key,value,expireDuration);
    }

    public void deleteData(String key){
        stringRedisTemplate.delete(key);
    }
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void setBlackList(String key, Object o, Long second) {
          stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + key, o.toString(), Duration.ofMillis(second));
    }

    public Object getBlackList(String key) {
        return stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + key);
    }

    public boolean deleteBlackList(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(REDIS_KEY_PREFIX + key));
    }

    public boolean hasKeyBlackList(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(REDIS_KEY_PREFIX + key));
    }


}