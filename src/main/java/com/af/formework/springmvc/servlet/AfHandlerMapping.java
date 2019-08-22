package com.af.formework.springmvc.servlet;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2019/8/22.
 */
@Data
public class AfHandlerMapping {

    private Object controller;

    private Method method;

    private Pattern pattern;

    public AfHandlerMapping(Pattern pattern,Object controller, Method method) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }
}
