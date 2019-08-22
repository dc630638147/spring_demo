package com.af.test;

import com.af.formework.context.AfApplicationContext;

/**
 * Created by Administrator on 2019/8/22.
 */
public class TestContext {
    public static void main(String[] args) {
        AfApplicationContext context = new AfApplicationContext("application.properties");
        context.refresh();
        Object ageService = context.getBean("demoActionController");
        System.out.println(ageService);
    }
}
