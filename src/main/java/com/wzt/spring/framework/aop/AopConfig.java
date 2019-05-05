package com.wzt.spring.framework.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


//匹配文件的目的：告诉spring,哪些类的哪些方法需要增强，增强的内容是什么
//对配置文件的所体现的内容进行封装
public class AopConfig {

    //以目标对象需要增强的Method作为key，需要增强的代码内容作为value
    private Map<Method,Aspect> points = new HashMap<>();

    public void put(Method target,Object aspect,Method[] points){
        this.points.put(target,new Aspect(aspect, points));
    }

    public Aspect get(Method method) {
        return this.points.get(method);
    }

    public boolean contains(Method method){
        return this.points.containsKey(method);
    }

    //对增强的代码的封装
    public class Aspect{
        private Object aspect;//代理对象
        private Method[] points;//代理的方法
        public Aspect(Object aspect,Method[] points){
            this.aspect = aspect;
            this.points = points;
        }

        public Object getAspect() {
            return aspect;
        }

        public Method[] getPoints() {
            return points;
        }
    }
}
