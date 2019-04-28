package com.wzt.spring.framework.test.service;

import com.wzt.spring.framework.annotation.Service;

@Service
public class TestServiceImpl implements TestService {
    @Override
    public void test() {
        System.out.println("TestServiceImpl.test()");
    }
}
