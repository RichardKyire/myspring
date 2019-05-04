package com.wzt.spring.framework.test.controller;

import com.wzt.spring.framework.annotation.Autowired;
import com.wzt.spring.framework.annotation.Controller;
import com.wzt.spring.framework.annotation.RequestMapping;
import com.wzt.spring.framework.test.service.TestService;
import com.wzt.spring.framework.webmvc.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    TestService testService;

    @RequestMapping("/sayHello")
    public ModelAndView helloWorld() {
//        testService.test();
        Map<String,Object> model = new HashMap<>();
        model.put("name","kyrie.wu");
        ModelAndView mv = new ModelAndView("first.html",model);
        return mv;
    }
}
