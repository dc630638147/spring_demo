package com.af.formework.context;

/**
 * 通过解耦方式获得IOC容器的顶层设计
 * 后面通过一个监听器去扫描所有的类，只要实现了此接口，从而将Ioc容器注入到目标类中
 */
public interface AfApplicationContextAware {
    void setApplicationContext(AfApplicationContext applicationContext);
}
