package com.wzt.spring.framework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// 默认使用JDK动态代理
public class AopProxy implements InvocationHandler {

  private AopConfig config;

  private Object target;

  public Object getProxy(Object instance) {

    this.target = instance;
    Class<?> clazz = instance.getClass();
    return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
  }

  public void setConfig(AopConfig config) {
    this.config = config;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      //method 是实际调用的方法，spring注入的是代理泪，所以这里是代理类的方法
    System.out.println("enter AopProxy.invoke");

    Method m = this.target.getClass().getMethod(method.getName(),method.getParameterTypes());

    // 在原始方法调用以前要执行增强的代码
    if (config.contains(m)) {
      AopConfig.Aspect aspect = config.get(m);
      aspect.getPoints()[0].invoke(aspect.getAspect());
    }

    // 反射调用原始方法
    Object result = method.invoke(this.target, args);

    // 在原始方法调用以后要执行增加的代码
    if (config.contains(m)) {
      AopConfig.Aspect aspect = config.get(m);
      aspect.getPoints()[1].invoke(aspect.getAspect());
    }

    // 返回最原始的返回值
    return result;
  }

  public void before() {
    System.out.println("before============");
  }

  public void after() {
    System.out.println("after=============");
  }
}
