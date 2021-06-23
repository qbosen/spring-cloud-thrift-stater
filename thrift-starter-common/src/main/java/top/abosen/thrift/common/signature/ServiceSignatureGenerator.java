package top.abosen.thrift.common.signature;

/**
 * @author qiubaisen
 * @date 2021/6/24
 */

@FunctionalInterface
public interface ServiceSignatureGenerator {
    String generate(ServiceSignature serviceSignature);
}
