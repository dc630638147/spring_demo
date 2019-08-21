package com.af.formework.context;

import com.af.formework.beans.AfBeanFactory;
import com.af.formework.beans.AfBeanWrapper;
import com.af.formework.beans.config.AfBeanDefinition;
import com.af.formework.beans.supports.AfBeanDefinitionReader;
import com.af.formework.beans.supports.AfDefaultListableBeanFactory;

import java.util.List;
import java.util.Map;

/**
 * IOC、DI、AOP、MVC
 */
public class AfApplicationContext extends AfDefaultListableBeanFactory implements AfBeanFactory {

    private String [] configLocations;

    private AfBeanDefinitionReader reader;

    public AfApplicationContext(String...configLocations){
        this.configLocations = configLocations;
    }


    @Override
    protected void refresh() {
        //1.定位，定位配置文件
        reader = new AfBeanDefinitionReader(configLocations);

        //2，加载配置文件，扫描相关的累，把它们封装成BeanDefinition
        List<AfBeanDefinition> beanDefinitions = reader.loadBeanDefinition();

        //3，注册，把配置信息放到容器里面(伪IOC容器)
        doRegisterBeanDefinition(beanDefinitions);

        //4,把不是延时加载的类，要提前初始化
        doAutoWired();
    }

    private void doAutoWired() {
        for(Map.Entry<String,AfBeanDefinition> entry : beanDefinitionMap.entrySet()){
            if(!entry.getValue().isLazyInit()){
                getBean(entry.getKey());
            }
        }
    }

    private void doRegisterBeanDefinition(List<AfBeanDefinition> beanDefinitions) {
        for(AfBeanDefinition beanDefinition : beanDefinitions){
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
    }


    @Override
    public Object getBean(String beanName) {
        //1.初始化
        instantiateBean(beanName,new AfBeanDefinition());
        //2.注入
        poputateBean(beanName,new AfBeanDefinition(),new AfBeanWrapper());
        //3.aop
        return null;
    }

    private void poputateBean(String beanName, AfBeanDefinition afBeanDefinition, AfBeanWrapper afBeanWrapper) {
    }

    private void instantiateBean(String beanName, AfBeanDefinition afBeanDefinition) {
    }
}
