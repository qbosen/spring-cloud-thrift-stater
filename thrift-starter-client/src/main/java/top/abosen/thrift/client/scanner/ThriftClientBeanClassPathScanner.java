package top.abosen.thrift.client.scanner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import top.abosen.thrift.client.exception.ThriftClientException;
import top.abosen.thrift.client.properties.ThriftClientProperties;
import top.abosen.thrift.common.Constants;
import top.abosen.thrift.common.Utils;
import top.abosen.thrift.common.ServiceSignature;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * @author qiubaisen
 * @date 2021/6/24
 */

@Slf4j
public class ThriftClientBeanClassPathScanner extends ClassPathBeanDefinitionScanner {

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

    private void handleBeanDefinitionHolders(Iterable<BeanDefinitionHolder> definitionHolders, ThriftClientProperties.Service service) {
        for (BeanDefinitionHolder definitionHolder : definitionHolders) {

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

            // 从 Iface 扫描的 version 都是 default_version
            ServiceSignature serviceSignature = new ServiceSignature(service.getServiceName(), serviceClass, Constants.DEFAULT_VERSION);

            definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.BEAN_CLASS, ifaceClass);
            definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.BEAN_CLASS_NAME, ifaceClass.getName());
            definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.SERVICE_CLASS, serviceClass);
            definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.SERVICE_SIGNATURE, serviceSignature);
            definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.CLIENT_CLASS, clientClass);
            definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.CLIENT_CONSTRUCTOR, constructor);
            definition.getPropertyValues().addPropertyValue(ThriftClientFactoryBean.CLIENT_CONFIG, service);


            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_NO);
            definition.setPrimary(true);
            definition.setBeanClass(ThriftClientFactoryBean.class);
            definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        }

    }

    public int scanService(ThriftClientProperties.Service service) {
        String[] packagesToScan = Arrays.stream(service.getPackageToScan().split(","))
                .map(String::trim).filter(StringUtils::isNoneEmpty).toArray(String[]::new);
        if (packagesToScan.length == 0) {
            log.warn("服务[{}] 未配置扫描包路径", service.getServiceName());
            return 0;
        }

        BeanDefinitionRegistry registry = getRegistry();
        Objects.requireNonNull(registry);
        int beanCountAtScanStart = registry.getBeanDefinitionCount();
        Set<BeanDefinitionHolder> definitionHolders = doScan(packagesToScan);
        handleBeanDefinitionHolders(definitionHolders, service);
        return getRegistry().getBeanDefinitionCount() - beanCountAtScanStart;
    }
}
