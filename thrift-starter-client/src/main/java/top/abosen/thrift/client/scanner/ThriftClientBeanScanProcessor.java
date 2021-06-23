package top.abosen.thrift.client.scanner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.ScopedProxyMode;
import top.abosen.thrift.client.properties.ThriftClientServiceProperties;

/**
 * @author qiubaisen
 * @date 2021/6/24
 */

@Slf4j
public class ThriftClientBeanScanProcessor implements ApplicationContextAware, BeanFactoryPostProcessor {

    private ApplicationContext applicationContext;

    @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinitionRegistry definitionRegistry = (BeanDefinitionRegistry) beanFactory;
        ThriftClientBeanClassPathScanner beanScanner = new ThriftClientBeanClassPathScanner(definitionRegistry);
        beanScanner.setResourceLoader(applicationContext);
        beanScanner.setBeanNameGenerator(new FullyQualifiedAnnotationBeanNameGenerator());
        beanScanner.setScopedProxyMode(ScopedProxyMode.INTERFACES);

//        setScannedPackages(beanScanner, applicationContext.getEnvironment().getProperty(SPRING_THRIFT_CLIENT_PACKAGE_TO_SCAN));

    }

}
