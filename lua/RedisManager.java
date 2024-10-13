package com.yiyouliao.autoprod.liaoyuan.lua;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author 陈阳
 * @Date 2023/10/9 19:07
 **/
@Component
public class RedisManager {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * @description  
     * @author  陈阳
     * @date    2023/10/9 19:08
     * @param    key    限流接口的id
     * @param    max    令牌桶的最大大小
     * @param    rate 令牌重置的时间
     * @return  boolean
    */
    public boolean rateLimit(String key, int max, int rate) {
        List<String> keyList = new ArrayList<>(1);
        keyList.add(key);
        return "1".equals(stringRedisTemplate
                .execute(new RedisReteLimitScript(), keyList, Integer.toString(max), Integer.toString(rate),
                        Long.toString(System.currentTimeMillis())));
    }
}
