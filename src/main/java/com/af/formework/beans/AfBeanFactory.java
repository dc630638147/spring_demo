package com.af.formework.beans;

/**
 * 单例工厂顶层设计
 */
public interface AfBeanFactory {

    /**
     * 根据beanName从IOC容器中获得一个实例Bean
     * @param beanName
     * @return
     */
    Object getBean(String beanName);
}
