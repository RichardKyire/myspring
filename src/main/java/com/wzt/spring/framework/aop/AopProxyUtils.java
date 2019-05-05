package com.wzt.spring.framework.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class AopProxyUtils {
    public static Object getTargetObject(Object proxy) throws Exception{
        //先判断一下，传进来的对象是不是一个代理过的对象
        if(!isAopProxy(proxy)){
            return  proxy;
        }
        return getProxyTargetObject(proxy);
    }

    private static boolean isAopProxy(Object obj){
        return Proxy.isProxyClass(obj.getClass());
    }

    private static Object getProxyTargetObject(Object proxy) throws NoSuchFieldException, IllegalAccessException {

        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);
        Field targetField = aopProxy.getClass().getDeclaredField("target");
        targetField.setAccessible(true);
        return targetField.get(aopProxy);
    }
}
