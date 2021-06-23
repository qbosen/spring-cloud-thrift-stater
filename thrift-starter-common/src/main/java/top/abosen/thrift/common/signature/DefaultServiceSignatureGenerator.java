package top.abosen.thrift.common.signature;

/**
 * @author qiubaisen
 * @date 2021/6/24
 */
public class DefaultServiceSignatureGenerator implements ServiceSignatureGenerator {
    @Override public String generate(ServiceSignature signature) {
        return String.format("%s:%s:%s", signature.getServiceName(), signature.getServiceClass().getName(), signature.getVersion());
    }
}
