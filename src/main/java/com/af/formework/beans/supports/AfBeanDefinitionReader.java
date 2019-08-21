package com.af.formework.beans.supports;

import com.af.formework.beans.config.AfBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 定位加载配置文件
 */
public class AfBeanDefinitionReader {

    private String [] locatons;

    private Properties config = new Properties();
    //配置文件中包路径key
    private final String SCAN_PACKAGE = "scanPackage";
    //类路径+名称集合
    private List<String> registyBeanClasses = new ArrayList<String>();



    public AfBeanDefinitionReader(String... locations){
        //通过URL定位找到其所对应的文件，然后转换为文件流
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:",""));
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //加载类
        doSanner(config.getProperty(SCAN_PACKAGE));
    }

    private void doSanner(String scanPackage) {
        //转换为文件路径，实际上就是把.替换为/就OK了
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if(file.isDirectory()){
                doSanner(scanPackage + "." + file.getName());
            }else{
                if(!file.getName().endsWith(".class")){ continue;}
                String className = (scanPackage + "." + file.getName().replace(".class",""));
                registyBeanClasses.add(className);
            }
        }
    }


    public List<AfBeanDefinition> loadBeanDefinition(){
        List <AfBeanDefinition> list = new ArrayList<>();
        try {
            for(String className : registyBeanClasses){
                AfBeanDefinition afBeanDefinition = doCreateBeanDefinition(className);
                if(afBeanDefinition == null){
                    continue;
                }
                list.add(afBeanDefinition);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    private AfBeanDefinition doCreateBeanDefinition(String className){
       try{
           Class<?> beanClass = Class.forName(className);
           if(beanClass.isInterface()){
               //忽略接口
               return null;
           }
           AfBeanDefinition bd = new AfBeanDefinition();
           bd.setBeanClassName(className);
           bd.setFactoryBeanName(beanClass.getSimpleName());
           return bd;
       }catch (Exception e){
           e.printStackTrace();
       }
        return null;
    }
}
