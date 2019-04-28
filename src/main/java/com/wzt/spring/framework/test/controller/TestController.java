package com.wzt.spring.framework.test.controller;

import com.wzt.spring.framework.annotation.Autowired;
import com.wzt.spring.framework.annotation.Controller;
import com.wzt.spring.framework.annotation.RequestMapping;
import com.wzt.spring.framework.test.service.TestService;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    TestService testService;

    @RequestMapping("/sayHello")
    public String helloWorld(){
        testService.test();
       return "helloWorld";
    }
}
