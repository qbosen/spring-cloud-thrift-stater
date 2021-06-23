package top.abosen.thrift.client.wrapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import top.abosen.thrift.common.Utils;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */


@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ThriftClientWrapper {
    String serviceName;
    String serviceSignature;
    Class<?> targetType;
    Class<?> ifaceType;
    Object target;
    double version;
    public static ThriftClientWrapper of(String serverId, String serviceName, Object thriftService, double version) {
        if (version <= 0) {
            throw new IllegalArgumentException("Thrift service version must be positive: " + version);
        }

        // todo support AsyncIface
        Class<?> ifaceType = Utils.findFirstInterface(thriftService.getClass(), iface -> iface.getName().endsWith("$Iface"))
                .orElseThrow(() -> new IllegalStateException("No thrift IFace found on service"));
        String signature = String.join("-", new String[]{serverId, serviceName, String.valueOf(version)});
        return new ThriftClientWrapper(serviceName, signature, thriftService.getClass(), ifaceType, thriftService, version);
    }
}
