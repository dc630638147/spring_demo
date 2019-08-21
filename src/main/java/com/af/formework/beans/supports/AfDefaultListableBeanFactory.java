package com.af.formework.beans.supports;

import com.af.formework.beans.config.AfBeanDefinition;
import com.af.formework.context.supports.AFAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2019/8/21.
 */
public class AfDefaultListableBeanFactory extends AFAbstractApplicationContext {

    //存储注册信息的beanDefinition
    protected final Map<String,AfBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
}
