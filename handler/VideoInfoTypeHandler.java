package com.yiyouliao.autoprod.liaoyuan.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiyouliao.autoprod.liaoyuan.entity.bo.VideoInfoBO;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 将Json转为指定实体类
 * @Author 陈阳
 * @Date 2023/7/3 20:43
 **/
@MappedTypes(VideoInfoBO.class)
public class VideoInfoTypeHandler extends BaseTypeHandler<VideoInfoBO> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, VideoInfoBO videoInfoBO, JdbcType jdbcType) throws SQLException {
        try {
            preparedStatement.setString(i, objectMapper.writeValueAsString(videoInfoBO));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse VideoInfoBO into JSON.", e);
        }
    }

    @Override
    public VideoInfoBO getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return parseJson(resultSet.getString(s));
    }

    @Override
    public VideoInfoBO getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return parseJson(resultSet.getString(i));
    }

    @Override
    public VideoInfoBO getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return parseJson(callableStatement.getString(i));
    }
    private VideoInfoBO parseJson(String json) {
        try {
            // 将 JSON 字符串转换为 List<FansDataInfo>
            return objectMapper.readValue(json, new TypeReference<VideoInfoBO>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON into List<FansDataInfo>.", e);
        }
    }
}