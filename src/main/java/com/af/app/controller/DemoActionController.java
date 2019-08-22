package com.af.app.controller;

import com.af.app.service.AgeService;
import com.af.app.service.DemoService;
import com.af.formework.annotation.AfAutowired;
import com.af.formework.annotation.AfController;
import com.af.formework.annotation.AfRequestMapper;
import com.af.formework.annotation.AfRequestParam;

import java.io.IOException;


@AfController
@AfRequestMapper("/demoAction")
public class DemoActionController {
    @AfAutowired
    private DemoService demoService;
    @AfAutowired
    private AgeService ageService;

    @AfRequestMapper("/getName")
    public void getName(@AfRequestParam("name") String name) throws IOException {
        System.out.println("=============getname"+name);
        System.out.println("resluut-"+demoService.getName());
       // return demoService.getName();
        //response.getWriter().write(demoService.getName());
    }

    public int getAge(){
        return ageService.getAge();
    }



}
