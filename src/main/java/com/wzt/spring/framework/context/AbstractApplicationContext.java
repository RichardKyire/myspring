package com.wzt.spring.framework.context;

public abstract class AbstractApplicationContext {

    protected void onRefresh() {

    }

    protected abstract void refreshBeanFactory();
}
