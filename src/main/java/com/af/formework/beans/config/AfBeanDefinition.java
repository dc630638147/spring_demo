package com.af.formework.beans.config;

import lombok.Data;

/**
 * IOC容器存放的配置信息
 */
@Data
public class AfBeanDefinition {
    private String beanClassName;
    private boolean lazyInit = true;
    private String  factoryBeanName;
}
