package com.fastcampus.projectboard.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
public class RedisUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    private final String REDIS_KEY_PREFIX = "logout";

    private final String EXPIRED_DURATION = "EXPIRE_DURATION";

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

    public void setBlackList(String key, Object o, int minutes) {
          stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + key, o.toString(), Duration.ofMinutes(minutes));
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