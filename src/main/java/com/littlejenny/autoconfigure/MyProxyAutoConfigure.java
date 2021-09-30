package com.littlejenny.autoconfigure;

import com.littlejenny.beanprocess.MyBeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyProxyAutoConfigure {
    public MyProxyAutoConfigure(){

    }
    @Bean
    public static MyBeanDefinitionRegistryPostProcessor myBeanDefinitionRegistryPostProcessor(){
        return new MyBeanDefinitionRegistryPostProcessor();
    }
}
