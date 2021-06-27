package top.abosen.thrift.server.wrapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import top.abosen.thrift.common.Utils;
import top.abosen.thrift.common.signature.ServiceSignature;
import top.abosen.thrift.common.signature.ServiceSignatureGenerator;
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

    ServiceSignature serviceSignature;

    public static ThriftServiceWrapper of(String serverName, Object thriftService, double version, ServiceSignatureGenerator signatureGenerator) {
        if (version <= 0) {
            throw new IllegalArgumentException("Thrift service version must be positive: " + version);
        }

        Class<?> ifaceType = Utils.findFirstInterface(thriftService.getClass(), iface -> iface.getName().endsWith("$Iface"))
                .orElseThrow(() -> new IllegalStateException("No thrift IFace found on service"));
        ServiceSignature serviceSignature = new ServiceSignature(serverName, ifaceType.getEnclosingClass(), version);
        String signature = signatureGenerator.generate(serviceSignature);
        return new ThriftServiceWrapper(signature, thriftService.getClass(), ifaceType, thriftService, serviceSignature);
    }
}
