package top.abosen.thrift.client.scanner;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import top.abosen.thrift.client.exception.ThriftClientException;
import top.abosen.thrift.client.properties.ThriftClientServiceProperties;
import top.abosen.thrift.common.Constants;
import top.abosen.thrift.common.Utils;
import top.abosen.thrift.common.signature.ServiceSignature;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Set;

/**
 * @author qiubaisen
 * @date 2021/6/24
 */

@Slf4j
public class ThriftClientBeanClassPathScanner extends ClassPathBeanDefinitionScanner {

    @Setter(AccessLevel.PROTECTED)
    private ThriftClientServiceProperties clientServiceProperties;

    public ThriftClientBeanClassPathScanner(BeanDefinitionRegistry registry) {
        super(registry, true);
    }

    @Override
    protected void registerDefaultFilters() {
        this.addIncludeFilter((metadataReader, metadataReaderFactory) ->
                metadataReader.getClassMetadata().isInterface() &&
                metadataReader.getClassMetadata().getClassName().endsWith("$Iface"));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isInterface();
    }


    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> definitionHolders = super.doScan(basePackages);
        definitionHolders.forEach(this::handleBeanDefinitionHolder);
        return definitionHolders;
    }

    private void handleBeanDefinitionHolder(BeanDefinitionHolder definitionHolder) {
        GenericBeanDefinition definition = (GenericBeanDefinition) definitionHolder.getBeanDefinition();

        if (StringUtils.isEmpty(definition.getBeanClassName()) || !definition.getBeanClassName().endsWith("$Iface")) {
            // 不是一个可行的 iface 定义
            return;
        }
        Class<?> ifaceClass;
        try {
            ifaceClass = Class.forName(definition.getBeanClassName());
        } catch (ClassNotFoundException e) {
            log.error("无法加载iface类定义", e);
            return;
        }
        // Iface接口的包裹累 就是 service 类
        Class<?> serviceClass = ifaceClass.getEnclosingClass();

        Class<? extends TServiceClient> clientClass = Arrays.stream(serviceClass.getClasses())
                .filter(it -> ClassUtils.isAssignable(TServiceClient.class, it))
                .findAny().map(Utils::<Class<? extends TServiceClient>>cast)
                .orElseThrow(() -> new ThriftClientException("未找到相关的Client定义"));

        Constructor<? extends TServiceClient> constructor;
        try {
            constructor = clientClass.getConstructor(TProtocol.class);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new ThriftClientException("未找到Client的TProtocol构造器", e);
        }

        if (clientServiceProperties == null) {
            throw new ThriftClientException("无法加载服务配置");
        }
        // 从 Iface 扫描的 version 都是 default_version
        ServiceSignature serviceSignature = new ServiceSignature(clientServiceProperties.getServiceName(), serviceClass, Constants.DEFAULT_VERSION);
        definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.BEAN_CLASS, ifaceClass);
        definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.BEAN_CLASS_NAME, ifaceClass.getName());
        definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.SERVICE_CLASS, serviceClass);
        definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.SERVICE_SIGNATURE, serviceSignature);
        definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.CLIENT_CLASS, clientClass);
        definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.CLIENT_CONSTRUCTOR, constructor);
        definition.setBeanClass(ThriftClientFactoryBean.class);
    }

}
