package top.abosen.thrift.server.server;

import com.ecwid.consul.v1.agent.model.NewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import top.abosen.thrift.server.properties.ThriftServerProperties;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author qiubaisen
 * @date 2021/6/26
 */

@Slf4j
public class ThriftServerConsulDiscovery {
    final ConsulRegistration consulRegistration;
    final ConsulServiceRegistry consulServiceRegistry;
    final ThriftServerProperties serverProperties;
    boolean registered = false;

    public ThriftServerConsulDiscovery(
            ThriftServerProperties serverProperties,
            ConsulServiceRegistry consulServiceRegistry,
            AutoServiceRegistrationProperties autoServiceRegistrationProperties,
            ConsulDiscoveryProperties discoveryProperties,
            ApplicationContext context,
            HeartbeatProperties heartbeatProperties
    ) {
        this.serverProperties = serverProperties;
        this.consulServiceRegistry = consulServiceRegistry;
        this.consulRegistration = createConsulRegistration(serverProperties, discoveryProperties, autoServiceRegistrationProperties, context, heartbeatProperties);
    }

    @EventListener(ThriftServerStartEvent.class)
    public void registerService() {
        if (!registered && serverProperties.getDiscovery().isRegister()) {
            consulServiceRegistry.register(consulRegistration);
            this.registered = true;
        }
    }

    @EventListener(ThriftServerStopEvent.class)
    public void unregisterService() {
        if (registered) {
            consulServiceRegistry.deregister(consulRegistration);
            log.debug("注销服务:{}", consulRegistration.getService().getId());
        }
    }

    private static ConsulRegistration createConsulRegistration(
            ThriftServerProperties serverProperties,
            ConsulDiscoveryProperties discoveryProperties,
            AutoServiceRegistrationProperties autoServiceRegistrationProperties,
            ApplicationContext context,
            HeartbeatProperties heartbeatProperties
    ) {
        NewService service = new NewService();
        service.setPort(serverProperties.getServicePort());
        service.setName(serverProperties.getServiceName());
        service.setEnableTagOverride(true);

        ThriftServerProperties.Discovery discoveryConfig = serverProperties.getDiscovery();
        service.setAddress(discoveryConfig.isPreferIpAddress() ?
                discoveryProperties.getIpAddress() : discoveryProperties.getHostname());
        service.setId(ConsulAutoRegistration.normalizeForDns(discoveryConfig.getInstanceId()));
        service.setTags(new ArrayList<>(discoveryConfig.getTags()));

        // 健康检测 tcp
        if (discoveryConfig.isHealthCheck()) {
            NewService.Check check = new NewService.Check();
            check.setTcp(service.getAddress() + ":" + service.getPort());
            check.setHeader(discoveryProperties.getHealthCheckHeaders());
            check.setInterval(discoveryProperties.getHealthCheckInterval());
            check.setTimeout(discoveryProperties.getHealthCheckTimeout());
            check.setTlsSkipVerify(discoveryProperties.getHealthCheckTlsSkipVerify());
            if (heartbeatProperties.isEnabled()) {
                check.setTtl(heartbeatProperties.getTtl().getSeconds() + "s");
            }

            service.setCheck(check);
        }

        return new ConsulAutoRegistration(
                service, autoServiceRegistrationProperties, discoveryProperties, context, heartbeatProperties, Collections.emptyList()
        );
    }

}
