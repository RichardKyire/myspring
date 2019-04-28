package com.wzt.spring.framework.context.support;

import com.wzt.spring.framework.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//用于对配置文件进行查找、读取、解析
public class BeanDefinitionReader {

    private Properties config = new Properties();

    private List<String> registyBeanClasses = new ArrayList<String>();

    //在配置文件中，用来获取自动扫描的包名的key
    private final String SCAN_PACKAGE = "scanPackage";

    public BeanDefinitionReader(String... locations) {

        //TODO 循环字符串数组
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));

        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    public List<String> loadBeanDefinitions() {
        return this.registyBeanClasses;
    }

    public List<String> getRegistryBeeanClasses() {
        return null;
    }

    //每注册一个className,就返回一个BeanDefinition,自己包装的
    public BeanDefinition registerBean(String className) {
        if (this.registyBeanClasses.contains(className)) {
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".") + 1)));
            return beanDefinition;
        }

        return null;
    }

    public Properties getConfig() {
        return this.config;
    }

    public String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    //递归扫描所有的相关联的额class,并且保存到一List中
    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                registyBeanClasses.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }
}
