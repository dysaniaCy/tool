package com.yiyouliao.autoprod.liaoyuan.interceptor;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import com.yiyouliao.autoprod.liaoyuan.utils.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * sql拦截，打印sql及耗时
 *
 * @author ld
 * @date 2022/6/21
 */
@Slf4j
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})})
public class MybatisSqlInterceptor extends AbstractSqlParserHandler implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        String sql = "";
        try {
            StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
            MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
            sql = boundSql.getSql().replaceAll("\\s+", " ");

            List<ParameterMapping> parameterMappings = new ArrayList<>(boundSql.getParameterMappings());
            Object parameterObject = boundSql.getParameterObject();
            if (parameterMappings.isEmpty() && parameterObject == null) {
                log.warn("parameterMappings is empty or parameterObject is null");
                return invocation.proceed();
            }

            Configuration configuration = mappedStatement.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            this.sqlParser(metaObject);

            String parameter = "null";
            MetaObject newMetaObject = configuration.newMetaObject(parameterObject);
            for (ParameterMapping parameterMapping : parameterMappings) {
                if (parameterMapping.getMode() == ParameterMode.OUT) {
                    continue;
                }
                String propertyName = parameterMapping.getProperty();
                if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    parameter = getParameterValue(parameterObject);
                } else if (newMetaObject.hasGetter(propertyName)) {
                    parameter = getParameterValue(newMetaObject.getValue(propertyName));
                } else if (boundSql.hasAdditionalParameter(propertyName)) {
                    parameter = getParameterValue(boundSql.getAdditionalParameter(propertyName));
                }

                sql = sql.replaceFirst("\\?", parameter);
            }
        } catch (Exception e) {
            log.error(String.format("[EXECUTE SQL] intercept sql error: [%s]", sql), e);
        } finally {
            if (LoggingUtils.printLogging()) {
                log.info("[EXECUTE SQL] cost:{}ms,sql:{}", System.currentTimeMillis() - startTime, sql);
            }
        }
        return invocation.proceed();
    }

    /**
     * 获取参数
     *
     * @param param Object类型参数
     * @return 转换之后的参数
     */
    private static String getParameterValue(Object param) {
        if (param == null) {
            return "null";
        }
        if (param instanceof Number) {
            return param.toString();
        }
        String value = null;
        if (param instanceof String) {
            value = param.toString();
        } else if (param instanceof Date) {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) param);
        } else if (param instanceof IEnum) {
            value = String.valueOf(((IEnum) param).getValue());
        } else {
            value = param.toString();
        }
        return StringUtils.quotaMark(value);
    }


    @Override
    public Object plugin(Object o) {
        if (o instanceof StatementHandler) {
            return Plugin.wrap(o, this);
        }
        return o;
    }

    @Override
    public void setProperties(Properties properties) {

    }

}

