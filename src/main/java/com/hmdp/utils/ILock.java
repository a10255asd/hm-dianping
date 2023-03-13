package com.hmdp.utils;

/**
 * @Author LiuJixue
 * @Date 2023/3/13 15:01
 * @PackageName:com.hmdp.utils
 * @ClassName: Ilock
 * @Description: TODO
 * @Version 1.0
 */
public interface ILock {
    /**
     * 尝试获取锁
     * @param timeoutSec 锁持有的超时时间，过期后自动释放
     * @return true 代表获取锁成功，false 代表获取锁失败
     */

    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
