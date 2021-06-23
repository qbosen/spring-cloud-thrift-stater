package top.abosen.thrift.client.scanner;

import org.apache.thrift.TServiceClient;
import top.abosen.thrift.common.signature.ServiceSignature;

import java.lang.reflect.Constructor;

/**
 * @author qiubaisen
 * @date 2021/6/24
 */
public class ThriftClientFactoryBean {
    public final static String BEAN_CLASS = "beanClass";
    public final static String BEAN_CLASS_NAME = "beanClassName";
    public static final String SERVICE_CLASS = "serviceClass";
    public final static String SERVICE_SIGNATURE = "serviceSignature";
    public final static String CLIENT_CLASS = "clientClass";
    public final static String CLIENT_CONSTRUCTOR = "clientConstructor";

    private Class<?> beanClass;
    private String beanClassName;
    private Class<?> serviceClass;
    private ServiceSignature serviceSignature;
    private Class<?> clientClass;
    private Constructor<? extends TServiceClient> clientConstructor;
}
