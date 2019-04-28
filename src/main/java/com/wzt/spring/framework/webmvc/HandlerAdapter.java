package com.wzt.spring.framework.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class HandlerAdapter {

    private Map<String ,Integer> paramMapping;

    public HandlerAdapter(Map<String ,Integer> paramMapping) {
        this.paramMapping = paramMapping;

    }

    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handler) {
        return null;
    }
}
