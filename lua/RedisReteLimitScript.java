package com.yiyouliao.autoprod.liaoyuan.lua;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * @Author 陈阳
 * @Date 2023/10/9 18:06
 **/

public class RedisReteLimitScript implements RedisScript<String> {

    private static final String SCRIPT = "local ratelimit_info = redis.pcall('HMGET',KEYS[1],'last_time','current_token')\n" +
        "local last_time = ratelimit_info[1]\n" +
        "local current_token = tonumber(ratelimit_info[2])\n" +
        "local max_token = tonumber(ARGV[1])\n" +
        "local reverse_time = tonumber(ARGV[2])\n" +
        "local current_time = tonumber(ARGV[3])\n" +
        "if current_token == nil then\n" +
        "  current_token = max_token\n" +
        "else\n" +
        "  local past_time = current_time-last_time\n" +
        "  if past_time>reverse_time then \n" +
        "    current_token = max_token\n" +
        "  end\n" +
        "end\n" +
        "local result = '0'\n" +
        "if(current_token>0) then\n" +
        "  result = '1'\n" +
        "  current_token = current_token-1\n" +
        "  last_time = current_time\n" +
        "end\n" +
        "redis.call('HMSET',KEYS[1],'last_time',last_time,'current_token',current_token)\n" +
        "redis.call('pexpire',KEYS[1],reverse_time)\n" +
        "return result";

    @Override   public String getSha1() {
        return DigestUtils.sha1Hex(SCRIPT);
    }

    @Override   public Class<String> getResultType() {
        return String.class;
    }

    @Override   public String getScriptAsString() {
        return SCRIPT;
    }
}


