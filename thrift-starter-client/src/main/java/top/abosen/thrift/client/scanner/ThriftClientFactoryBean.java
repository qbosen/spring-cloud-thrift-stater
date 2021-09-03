package top.abosen.thrift.client.scanner;

import lombok.Data;
import org.apache.thrift.TServiceClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import top.abosen.thrift.client.exception.ThriftClientException;
import top.abosen.thrift.client.properties.ThriftClientProperties;
import top.abosen.thrift.common.ServiceSignature;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;

/**
 * @author qiubaisen
 * @date 2021/6/24
 */

@Data
public class ThriftClientFactoryBean<T> implements FactoryBean<T>, InitializingBean {
    public static final String BEAN_CLASS = "beanClass";
    public static final String BEAN_CLASS_NAME = "beanClassName";
    public static final String SERVICE_CLASS = "serviceClass";
    public static final String SERVICE_SIGNATURE = "serviceSignature";
    public static final String CLIENT_CLASS = "clientClass";
    public static final String CLIENT_CONSTRUCTOR = "clientConstructor";
    public static final String CLIENT_CONFIG = "serviceConfig";

    private Class<T> beanClass;
    private String beanClassName;
    private Class<?> serviceClass;
    private ServiceSignature serviceSignature;
    private Class<?> clientClass;
    private Constructor<? extends TServiceClient> clientConstructor;
    private ThriftClientProperties.Service serviceConfig;


    @Override
    @SuppressWarnings("unchecked")
    public T getObject() throws Exception {
        if (beanClass.isInterface()) {
            return (T) Proxy.newProxyInstance(beanClass.getClassLoader(),
                    new Class<?>[]{beanClass},
                    new ThriftClientInvocationHandler(
                            serviceSignature,
                            clientConstructor,
                            serviceConfig
                    ));
        }

        throw new ThriftClientException(String.format("无法代理[%s]", beanClass.getName()));
    }

    @Override public Class<?> getObjectType() {
        return beanClass;
    }

    @Override public void afterPropertiesSet() throws Exception {
    }

}
