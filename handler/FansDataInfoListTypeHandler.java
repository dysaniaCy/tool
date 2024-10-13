package com.yiyouliao.autoprod.liaoyuan.handler;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiyouliao.autoprod.liaoyuan.thirdparty.douyin.model.FansDataInfo;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 将Json转为指定实体类
 * @Author 陈阳
 * @Date 2023/7/3 20:43
 **/
@MappedTypes(List.class)
public class FansDataInfoListTypeHandler extends BaseTypeHandler<List<FansDataInfo>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<FansDataInfo> parameter, JdbcType jdbcType)
            throws SQLException {
        // 将 List<FansDataInfo> 转换为 JSON 字符串并设置到 PreparedStatement 中
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse List<FansDataInfo> into JSON.", e);
        }
    }

    @Override
    public List<FansDataInfo> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 从 ResultSet 中获取 JSON 字符串并转换为 List<FansDataInfo>
        return parseJson(rs.getString(columnName));
    }

    @Override
    public List<FansDataInfo> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 从 ResultSet 中获取 JSON 字符串并转换为 List<FansDataInfo>
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public List<FansDataInfo> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 从 CallableStatement 中获取 JSON 字符串并转换为 List<FansDataInfo>
        return parseJson(cs.getString(columnIndex));
    }

    private List<FansDataInfo> parseJson(String json) {
        try {
            // 将 JSON 字符串转换为 List<FansDataInfo>
            if (StrUtil.isEmpty(json)) {
                return null;
            }
            return objectMapper.readValue(json, new TypeReference<List<FansDataInfo>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON into List<FansDataInfo>.", e);
        }
    }
}