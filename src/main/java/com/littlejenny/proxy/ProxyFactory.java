package com.littlejenny.proxy;
import com.littlejenny.annotation.MyAnnotation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyFactory {
    private Class target;
    private MyAnnotation myAnnotation;
    public ProxyFactory(Class target, MyAnnotation myAnnotation) {
        this.myAnnotation = myAnnotation;
        this.target = target;
    }

    public Object getProxyInstance(){
        return Proxy.newProxyInstance(target.getClassLoader(), new Class[]{target}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return myAnnotation.value();
            }
        });
    }
}
