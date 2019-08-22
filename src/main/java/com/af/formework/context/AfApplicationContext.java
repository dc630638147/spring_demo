package com.af.formework.context;

import com.af.formework.annotation.AfAutowired;
import com.af.formework.annotation.AfController;
import com.af.formework.annotation.AfService;
import com.af.formework.beans.AfBeanFactory;
import com.af.formework.beans.AfBeanWrapper;
import com.af.formework.beans.config.AfBeanDefinition;
import com.af.formework.beans.supports.AfBeanDefinitionReader;
import com.af.formework.beans.supports.AfDefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IOC、DI、AOP、MVC
 */
public class AfApplicationContext extends AfDefaultListableBeanFactory implements AfBeanFactory {

    private String [] configLocations;

    private AfBeanDefinitionReader reader;

    private Map<String,Object> singletonObjects = new ConcurrentHashMap<>();

    private Map<String,AfBeanWrapper> factoryBeanInstanceWrapper = new ConcurrentHashMap<>();


    public AfApplicationContext(String...configLocations){
        this.configLocations = configLocations;
    }



    @Override
    public void refresh() {
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

        AfBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if(beanDefinition == null){
            return null;
        }

        //1.初始化
        Object instance = instantiateBean(beanName, beanDefinition);

        //封装beanWrapper
        AfBeanWrapper beanWrapper = new AfBeanWrapper(instance);

        //拿到beanWrapper后，将beanWrapper保存到IOC容器中。这是真正的IOC容器
        factoryBeanInstanceWrapper.put(beanName,beanWrapper);

        //2.注入
        poputateBean(beanName,beanDefinition,beanWrapper);
        return factoryBeanInstanceWrapper.get(beanName).getWrapperInstance();
    }

    private void poputateBean(String beanName, AfBeanDefinition beanDefinition, AfBeanWrapper beanWrapper) {
        //只有加了注解的类
        Object instance = beanWrapper.getWrapperInstance();
        Class<?> clazz = instance.getClass();
        if(!(clazz.isAnnotationPresent(AfController.class) || clazz.isAnnotationPresent(AfService.class))){
            return;
        }

        //获取该类下所有的Fieldsc
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields){
            //判断字段有没有加注解
            if(!field.isAnnotationPresent(AfAutowired.class)){
                continue;
            }
            AfAutowired annotation = field.getAnnotation(AfAutowired.class);

            String autoBeanName = annotation.value().trim();
            if("".equals(autoBeanName)){
                autoBeanName = toLowerFirstCase(field.getType().getSimpleName());
            }
            //强吻
            field.setAccessible(true);
            if(this.factoryBeanInstanceWrapper.get(autoBeanName) == null){
                continue;
            }
            try {
                field.set(instance,factoryBeanInstanceWrapper.get(beanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    private Object instantiateBean(String beanName, AfBeanDefinition afBeanDefinition) {
        //1、拿到实例化的类名
        String beanClassName = afBeanDefinition.getBeanClassName();
        //2、反射实例化,先从缓存中获取,缓存没有在创建
        Object instance = null;
        try {
            //默认就是单例，从单例的缓存ioc中获取。
            if(singletonObjects.containsKey(beanClassName)){
                instance = singletonObjects.get(beanClassName);
            }else{
                //创建 添加
                Class<?> clazz = Class.forName(beanClassName);
                instance = clazz.newInstance();
                //添加进缓存
                singletonObjects.put(beanClassName,instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }


    public String [] getBeanNames(){
        Set<String> strings = this.beanDefinitionMap.keySet();
        return strings.toArray(new String[strings.size()]);
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        //之所以加，是因为大小写字母的ASCII码相差32，
        // 而且大写字母的ASCII码要小于小写字母的ASCII码
        //在Java中，对char做算学运算，实际上就是对ASCII码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }
}
