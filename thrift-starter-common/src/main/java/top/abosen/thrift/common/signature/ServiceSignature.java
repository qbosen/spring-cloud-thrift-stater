package top.abosen.thrift.common.signature;

import lombok.Value;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */

@Value
public class ServiceSignature {
    String serviceName;
    Class<?> serviceClass;
    double version;

}
