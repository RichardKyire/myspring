package com.wzt.spring.framework.webmvc;

import java.io.File;

public class ViewResolver {
    private String name;
    private File template;
    public ViewResolver(String name, File template) {
        this.name = name;
        this.template = template;
    }
}
