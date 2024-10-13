package com.yiyouliao.autoprod.liaoyuan.interceptor;

import cn.hutool.core.util.StrUtil;
import com.yiyouliao.autoprod.liaoyuan.annotation.ProdJobTag;
import com.yiyouliao.autoprod.liaoyuan.annotation.ProdJobTagIgnore;
import com.yiyouliao.autoprod.liaoyuan.entity.domain.ProdJobDO;
import com.yiyouliao.autoprod.liaoyuan.entity.domain.ProdJobParamDO;
import com.yiyouliao.autoprod.liaoyuan.service.ProdJobParamService;
import com.yiyouliao.autoprod.liaoyuan.service.impl.ProdJobParamServiceImpl;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Map;

/**
 * @author 陈阳
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class ProdJobInterceptor implements Interceptor {

    private final ApplicationContext applicationContext;

    public ProdJobInterceptor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String originalSql = boundSql.getSql();
//
//        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
//
//        // 分离代理对象链
//        while (metaObject.hasGetter("h")) {
//            Object obj = metaObject.getValue("h");
//            metaObject = SystemMetaObject.forObject(obj);
//        }
//
//        while (metaObject.hasGetter("target")) {
//            Object obj = metaObject.getValue("target");
//            metaObject = SystemMetaObject.forObject(obj);
//        }
//
//        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        // 对原始 SQL 进行判断和处理
        if ((originalSql.trim().toUpperCase().contains("INSERT") || originalSql.trim().toUpperCase().contains("UPDATE"))
                && originalSql.trim().toLowerCase().contains("ly_prod_job ")) {
            // 获取参数
            ParameterHandler parameterHandler = statementHandler.getParameterHandler();
            Object parameterObject = parameterHandler.getParameterObject();

            // 将生产作业表的动态参数数据存放到生产作业参数表
            if (parameterObject instanceof ProdJobDO) {
                saveDataParam((ProdJobDO) parameterObject);
            }

            if (parameterObject instanceof Map) {
                Map parameterMap = (Map) parameterObject;
                for (Object key : parameterMap.keySet()) {
                    Object value = parameterMap.get(key);
                    if (value instanceof ProdJobDO) {
                        saveDataParam((ProdJobDO) value);
                    }
                }
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 是否标记为区域字段
     * @return
     */
    public static boolean isAreaTag(MappedStatement mappedStatement) throws ClassNotFoundException {
        String id = mappedStatement.getId();
        if (!id.contains("ProdJobMapper")) {
            return false;
        }
        Class<?> classType = Class.forName(id.substring(0, mappedStatement.getId().lastIndexOf(".")));

        //获取对应拦截方法名
        String mName = mappedStatement.getId().substring(mappedStatement.getId().lastIndexOf(".") + 1);

        boolean ignore = false;

        for (Method method : classType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ProdJobTagIgnore.class) && mName.equals(method.getName())) {
                ignore = true;
            }
        }

        if (classType.isAnnotationPresent(ProdJobTag.class) && !ignore) {
            return true;
        }
        return false;
    }

    private void saveDataParam(ProdJobDO prodJobDO) {
        if (StrUtil.isNotEmpty(prodJobDO.getDataParam())) {
            ProdJobParamService prodJobParamService = applicationContext.getBean(ProdJobParamServiceImpl.class);
            ProdJobParamDO prodJobParamDO = prodJobParamService.getParamByJobId(prodJobDO.getJobId());
            if (prodJobParamDO != null) {
                prodJobParamDO.setDataParam(prodJobDO.getDataParam());
            } else {
                prodJobParamDO = new ProdJobParamDO();
                prodJobParamDO.setJobId(prodJobDO.getJobId());
                prodJobParamDO.setDataParam(prodJobDO.getDataParam());
//                // 提取每个镜头使用的素材lId
//                ScriptInstanceBO instance = JSONUtil.toBean(prodJobParamDO.getDataParam(), ScriptInstanceBO.class);
//                List<String> materialIdList = Lists.newArrayList();
//                for (ScriptCardBO card : instance.getCardList()) {
//                    for (ScriptSceneBO scene : card.getSceneList()) {
//                        for (ScriptComponentBO component : scene.getComponentList()) {
//                            if (ComponentType.VIDEO.equals(component.getComponentType())) {
//                                materialIdList.add(JSONUtil.parseObj(component.getConfig()).getStr("materialId"));
//                            }
//                        }
//                    }
//                }
//                // 记录下使用的素材id
//                prodJobParamDO.setMatxxerialIdList(materialIdList);
            }
            prodJobParamService.saveOrUpdate(prodJobParamDO);
            prodJobDO.setDataParam(StrUtil.EMPTY);
        }
    }
}
