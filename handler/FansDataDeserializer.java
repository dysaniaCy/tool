package com.yiyouliao.autoprod.liaoyuan.handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.yiyouliao.autoprod.liaoyuan.thirdparty.douyin.model.FansData;

import java.lang.reflect.Type;

/**
 * 粉丝数据实体类自定义反序列化对象策略
 * @Author 陈阳
 * @Date 2023/7/3 17:14
 **/

public class FansDataDeserializer implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONObject object = parser.parseObject();
        FansData fansData = null;
        //当JSON为空或者只有{}时，直接让实体类为空而不是空对象
        if (object != null && !object.toString().equals("{}")) {
            fansData = object.toJavaObject(FansData.class);
        }
        return (T) fansData;
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }
}
