package com.littlejenny.beanprocess;
import com.littlejenny.PackageUtils;
import com.littlejenny.annotation.MyAnnotation;
import com.littlejenny.proxy.ProxyFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

@Slf4j
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info("Invoke Metho postProcessBeanFactory");
//        这里可以设置属性，例如
//        BeanDefinition bd = beanFactory.getBeanDefinition("shanhyBB");
//        MutablePropertyValues mpv = bd.getPropertyValues();
//        mpv.addPropertyValue("sex", "male");

    }

    @SneakyThrows
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("Invoke Metho postProcessBeanDefinitionRegistry");
        List<String> strings = PackageUtils.jarDoScan("com.demo.reflection.myinterface", MyBeanDefinitionRegistryPostProcessor.class.getClassLoader());
        for (String string : strings) {
            log.debug("file {} 被我處理",string);
            Class<?> clazz = Class.forName(string);
            for (Annotation annotation : clazz.getDeclaredAnnotations()) {
                log.debug("file {} 有的 annotation為 {}",string,annotation.toString());
                if(annotation instanceof MyAnnotation){
                    log.debug("class {} 被我動態代理",clazz.getName());
                    MyAnnotation myAnnotation = (MyAnnotation) annotation;
                    ProxyFactory factory = new ProxyFactory(clazz,myAnnotation);
                    Object proxyInstance = factory.getProxyInstance();
                    //提供給Bean建構函數用
                    InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxyInstance);
                    registerBean(registry, clazz.getSimpleName(), proxyInstance.getClass(),invocationHandler);
                }
            }
        }
    }
    private void registerBean(BeanDefinitionRegistry registry, String name, Class<?> beanClass,InvocationHandler invocationHandler){

        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
        //因為動態代理產生的類只有一個帶參構造器(invocationHandler)，所以IOC會在容器中找invocationHandler，如果找不到會報錯
        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        constructorArgumentValues.addGenericArgumentValue(invocationHandler);
        abd.setConstructorArgumentValues(constructorArgumentValues);
        //Bean是為singleton還是multi
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        //如果沒設定bean名稱，就用類名
        String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, registry));
        //做一些基礎設定
//        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
    }
}
