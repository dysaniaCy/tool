package com.yiyouliao.autoprod.liaoyuan.handler;

import com.alibaba.fastjson.TypeReference;
import com.yiyouliao.autoprod.liaoyuan.entity.typehandler.ListTypeHandler;
import com.yiyouliao.autoprod.liaoyuan.thirdparty.arithmetic.model.Module;

import java.util.List;

/**
 * @author hongmao.xi
 * @date 2022-04-12 17:49
 **/
public class ModulesListTypeHandler extends ListTypeHandler<Module> {

    @Override
    protected TypeReference<List<Module>> specificType() {
        return new TypeReference<List<Module>>() {};
    }
}
