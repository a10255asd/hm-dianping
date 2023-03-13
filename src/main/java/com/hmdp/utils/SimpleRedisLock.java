package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @Author LiuJixue
 * @Date 2023/3/13 15:00
 * @PackageName:com.hmdp.utils
 * @ClassName: SimpleRedisLock
 * @Description: TODO
 * @Version 1.0
 */
public class SimpleRedisLock implements ILock{

    private static final String KEY_PREFIX = "lock:";

    private String name;
    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取线程的标识
        long threadId = Thread.currentThread().getId();
        // 获取锁
       Boolean success =  stringRedisTemplate.opsForValue()
               .setIfAbsent(KEY_PREFIX + name,threadId + "",timeoutSec, TimeUnit.SECONDS);
       return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        // 释放锁
        stringRedisTemplate.delete(KEY_PREFIX + name);
    }
}
