package top.abosen.thrift.server.wrapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import top.abosen.thrift.common.ServiceSignature;
import top.abosen.thrift.common.Utils;
import top.abosen.thrift.server.annotation.ThriftService;

/**
 * thrift 服务包装类
 *
 * @author qiubaisen
 * @date 2021/6/15
 */

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ThriftServiceWrapper {
    /**
     * 被wrapper对象的源类型 (代理前)， 即被 {@link ThriftService} 注解的类
     */
    Class<?> targetType;
    /**
     * 服务对应的thrift接口
     */
    Class<?> ifaceType;
    /**
     * thrift服务实现类
     */
    Object target;
    /**
     * 被spring托管的bean
     */
    Object bean;
    double version;


    public static ThriftServiceWrapper of(Object thriftService, Object bean, double version) {
        if (version <= 0) {
            throw new IllegalArgumentException("Thrift service version must be positive: " + version);
        }

        Class<?> ifaceType = Utils.findFirstInterface(thriftService.getClass(), iface -> iface.getName().endsWith("$Iface"))
                .orElseThrow(() -> new IllegalStateException("No thrift IFace found on service"));
        return new ThriftServiceWrapper(thriftService.getClass(), ifaceType, thriftService, bean, version);
    }

    /**
     * ThriftServiceWrapper 是业务信息，与服务配置、服务名解绑。达到同样的业务应用在不同服务的目的（同样的业务应用在不同的服务端协议）
     */
    public ServiceSignature serviceSignature(String serverName) {
        return new ServiceSignature(serverName, ifaceType.getEnclosingClass(), version);
    }
}
