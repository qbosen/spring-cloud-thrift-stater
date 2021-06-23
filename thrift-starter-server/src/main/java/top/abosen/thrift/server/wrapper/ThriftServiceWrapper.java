package top.abosen.thrift.server.wrapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
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
     * {@link ThriftService#value()} 服务名称/bean名称
     */
    String name;
    /**
     * 服务签名，用于唯一标示一个服务
     */
    String signature;
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
     * 版本号
     */
    double version;


    public static ThriftServiceWrapper of(String serverId, String serviceName, Object thriftService, double version) {
        if (version <= 0) {
            throw new IllegalArgumentException("Thrift service version must be positive: " + version);
        }

        // todo support AsyncIface
        Class<?> ifaceType = Utils.findFirstInterface(thriftService.getClass(), iface -> iface.getName().endsWith("$Iface"))
                .orElseThrow(() -> new IllegalStateException("No thrift IFace found on service"));
        String signature = String.join("-", new String[]{serverId, serviceName, String.valueOf(version)});
        return new ThriftServiceWrapper(serviceName, signature, thriftService.getClass(), ifaceType, thriftService, version);
    }
}
