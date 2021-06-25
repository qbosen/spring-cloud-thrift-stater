package top.abosen.thrift.client.scanner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.core.env.Environment;
import top.abosen.thrift.client.properties.ThriftClientProperties;

/**
 * @author qiubaisen
 * @date 2021/6/24
 */

@Slf4j
@RequiredArgsConstructor
public class ThriftClientBeanScanProcessor implements BeanFactoryPostProcessor, EnvironmentAware, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private ThriftClientProperties clientProperties;

    public static ApplicationContext applicationContext2;

    @Override
    public void setEnvironment(Environment environment) {
        // 此时 configurationProperties 尚未注册
        this.clientProperties = Binder.get(environment)
                .bind("spring.cloud.thrift.client", ThriftClientProperties.class).get();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        applicationContext2 = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinitionRegistry definitionRegistry = (BeanDefinitionRegistry) beanFactory;
        ThriftClientBeanClassPathScanner beanScanner = new ThriftClientBeanClassPathScanner(definitionRegistry);
        beanScanner.setResourceLoader(applicationContext);
        beanScanner.setBeanNameGenerator(new FullyQualifiedAnnotationBeanNameGenerator());
        scanThriftServices(beanScanner, clientProperties);
    }

    private void scanThriftServices(ThriftClientBeanClassPathScanner beanScanner, ThriftClientProperties clientProperties) {
        clientProperties.getServices().forEach(beanScanner::scanService);
    }

}
