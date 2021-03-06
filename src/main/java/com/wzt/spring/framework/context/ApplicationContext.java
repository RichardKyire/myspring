package com.wzt.spring.framework.context;

import com.wzt.spring.framework.annotation.Autowired;
import com.wzt.spring.framework.annotation.Controller;
import com.wzt.spring.framework.annotation.Service;
import com.wzt.spring.framework.aop.AopConfig;
import com.wzt.spring.framework.aop.AopProxyUtils;
import com.wzt.spring.framework.beans.BeanDefinition;
import com.wzt.spring.framework.beans.BeanPostProcessor;
import com.wzt.spring.framework.beans.BeanWrapper;
import com.wzt.spring.framework.context.support.BeanDefinitionReader;
import com.wzt.spring.framework.core.BeanFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory {

  private String[] configLocations;

  private BeanDefinitionReader reader;

  // 用来保证注册式单例的容器
  private Map<String, Object> beanCahceMap = new HashMap<>();

  // 用来存储所有的被代理过的对象
  private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();

  public ApplicationContext(String... configLocations) {
    this.configLocations = configLocations;
    refresh();
  }

  public void refresh() {

    // 定位
    this.reader = new BeanDefinitionReader(configLocations);

    // 加载
    List<String> beanNames = this.reader.loadBeanDefinitions();

    // 注册
    doRegistry(beanNames);

    // 依赖注入（lazy-init=false）,要执行依赖注入
    // 在这里自动调用getBean方法
    doAutowired();
  }

  private void populateBean(String beanName, Object instance) throws Exception {

    Class clazz = instance.getClass();
    if (!(clazz.isAnnotationPresent(Controller.class)
        || clazz.isAnnotationPresent(Service.class))) {
      return;
    }

    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (!field.isAnnotationPresent(Autowired.class)) {
        continue;
      }

      Autowired autowired = field.getAnnotation(Autowired.class);
      String autowiredBeanName = autowired.value().trim();
      if ("".equals(autowiredBeanName)) {
        autowiredBeanName = reader.lowerFirstCase(field.getType().getSimpleName());
      }

      field.setAccessible(true);
      try {
        field.set(instance, getBean(autowiredBeanName));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  private void doAutowired() {

    for (Map.Entry<String, BeanDefinition> beanDefinitionEntry :
        this.beanDefinitionMap.entrySet()) {
      String beanName = beanDefinitionEntry.getKey();
      if (!beanDefinitionEntry.getValue().isLazyInit()) {
        Object bean = getBean(beanName);
        System.out.println(String.format("spring produce bean clazz:" + bean.getClass()));
      }
    }
  }

  private void doRegistry(List<String> beanDefinitions) {
    try {
      for (String className : beanDefinitions) {

        // beanName有三种情况
        // 1、默认是类名首字母
        // 2、自定义
        // 3、接口注入
        Class<?> beanClass = Class.forName(className);

        // 如果是一个接口，是不能实例化的
        // 用它实现类来实例化
        if (beanClass.isInterface()) {
          continue;
        }

        BeanDefinition beanDefinition = reader.registerBean(className);
        if (beanDefinition != null) {
          this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }

        Class<?>[] interfaces = beanClass.getInterfaces();
        for (Class<?> i : interfaces) {
          // 如果是多个实现类，只能覆盖
          // 为什么？因为Spring没那么智能
          // 这个时候，可以自定义名字
          this.beanDefinitionMap.put(reader.lowerFirstCase(i.getSimpleName()), beanDefinition);
        }

        // 到这里为止，容器初始化完毕

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // 依赖注入，从这里开始，通过读取BeanDefinition中的信息
  // 然后，通过反射机制创建一个实例，并返回
  // Spring做法是，不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
  // 装饰器模式：
  // 1、保留原来的OOP关系
  // 2、需要对它进行扩展，增强（为了以后的AOP打基础）
  @Override
  public Object getBean(String beanName) {

    BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

    String className = beanDefinition.getBeanClassName();

    try {

      // 生成通知事件
      BeanPostProcessor beanPostProcessor = new BeanPostProcessor();

      Object instance = instantionBean(beanDefinition);
      if (null == instance) {
        return null;
      }

      // 在实例初始化以前调用一次
      beanPostProcessor.postProcessBeforeInitialization(instance, beanName);

      BeanWrapper beanWrapper = new BeanWrapper(instance);
      beanWrapper.setAopConfig(instantiateAopConfig(beanDefinition));
      beanWrapper.setBeanPostProcessor(beanPostProcessor);

      this.beanWrapperMap.put(beanName, beanWrapper);

      // 在实例初始化以后调用一次
      beanPostProcessor.postProcessAfterInitialization(instance, beanName);

      populateBean(beanName, instance);

      // 通过调用获取wrapper,相当于给我们自己留有了可操作的空间
      return this.beanWrapperMap.get(beanName).getWrappedInstance();

    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;

  }

  // 传一个BeanDefiniton,就返回一个实例Bean
  private Object instantionBean(BeanDefinition beanDefinition) {

    Object instance = null;

    String className = beanDefinition.getBeanClassName();
    try {
      Class<?> clazz = Class.forName(className);

      // 因为根据class才能确定一个类是否有实例
      if (this.beanCahceMap.containsKey(className)) {
        instance = this.beanCahceMap.get(className);
      } else {
        instance = clazz.newInstance();
        this.beanCahceMap.put(className, instance);
      }
      return instance;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String[] getBeanDefinitionNames() {
    return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
  }

  public int getBeanDefinitionCount() {
    return this.beanDefinitionMap.size();
  }

  public Properties getConfig() {
    return this.reader.getConfig();
  }

  @Override
  protected void refreshBeanFactory() {}

  private AopConfig instantiateAopConfig(BeanDefinition beanDefinition)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException,
          NoSuchMethodException {

    AopConfig config = new AopConfig();

    // public .* com\.wzt\.spring\.framework\.test\.service\..*Service.*\..*\(.*\)
    String expression = reader.getConfig().getProperty("pointCut");
    String[] before = reader.getConfig().getProperty("aspectBefore").split("\\s");
    String[] after = reader.getConfig().getProperty("aspectAfter").split("\\s");

    String className = beanDefinition.getBeanClassName();
    Class<?> clazz = Class.forName(className);
    Pattern pattern = Pattern.compile(expression);

    Class aspectClass = Class.forName(before[0]);
    for (Method method : clazz.getMethods()) {
      // method.toString()方法返回样例：
      // public void com.wzt.spring.framework.test.service.TestServiceImpl.test()
      Matcher matcher = pattern.matcher(method.toString());
      if (matcher.matches()) {
        // 能满足切面规则的类，添加到Aop配置中
        config.put(
            method,
            aspectClass.newInstance(),
            new Method[] {aspectClass.getMethod(before[1]), aspectClass.getMethod(after[1])});
      }
    }

    return config;
  }
}
