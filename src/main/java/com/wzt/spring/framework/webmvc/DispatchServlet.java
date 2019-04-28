package com.wzt.spring.framework.webmvc;


import com.wzt.spring.framework.context.ApplicationContext;
import com.wzt.spring.framework.test.service.TestService;
import javafx.application.Application;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

//Servlet只是作为MVC的入口
public class DispatchServlet extends HttpServlet {

    public static final String LOCATION = "contextConfigLocation";
    private Properties contextConfig = new Properties();

    private Map<String, Object> iocMap = new ConcurrentHashMap<>();

    private List<String> clazzNames = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        ApplicationContext applicationContext = new ApplicationContext(config.getInitParameter(LOCATION));
        TestService testService = (TestService) applicationContext.getBean("testService");
        testService.test();
        super.init(config);
    }
}
