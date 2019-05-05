package com.wzt.spring.framework.beans;

import com.wzt.spring.framework.aop.AopConfig;
import com.wzt.spring.framework.aop.AopProxy;
import com.wzt.spring.framework.core.FactoryBean;

public class BeanWrapper extends FactoryBean {

    private AopProxy aopProxy = new AopProxy();

    //还会用到  观察者  模式
    //1、支持事件响应，会有一个监听
    private BeanPostProcessor beanPostProcessor;

    private Object wrappedInstance;
    //原始的通过反射new出来，要把它包装起来，存下来
    private Object originalInstance;
    public BeanWrapper(Object instance){
            this.wrappedInstance = aopProxy.getProxy(instance);
            this.originalInstance = instance;
    }

    public Object getWrappedInstance(){
        return this.wrappedInstance;
    }

    //返回代理以后的Class
    //可能会是这个$Proxy0
    public Class<?> getWrappedClass(){
        return this.wrappedInstance.getClass();
    }

    public BeanPostProcessor getBeanPostProcessor() {
        return beanPostProcessor;
    }

    public void setBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessor = beanPostProcessor;
    }

    public void setWrappedInstance(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getOriginalInstance() {
        return originalInstance;
    }

    public void setOriginalInstance(Object originalInstance) {
        this.originalInstance = originalInstance;
    }

    public void setAopConfig(AopConfig config){
        aopProxy.setConfig(config);
    }
}
