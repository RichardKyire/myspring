package com.wzt.spring.framework.context;

import com.wzt.spring.framework.beans.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultListableBeanFactory extends AbstractApplicationContext {

    //beanDefinitionMap用来保存配置信息
    protected Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    @Override
    protected void onRefresh(){

    }

    @Override
    protected void refreshBeanFactory() {

    }
}
