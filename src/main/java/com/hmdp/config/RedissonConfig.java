package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @Author LiuJixue
 * @Date 2023/3/14 10:14
 * @PackageName:com.hmdp.config
 * @ClassName: RedissonConfig
 * @Description: TODO
 * @Version 1.0
 */
@Component
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://101.42.50.241:6379").setPassword("liuyiwei1A");
        // 创建redisson对象
        return Redisson.create(config);
    }
}
