package top.abosen.thrift.client.exception;

/**
 * @author qiubaisen
 * @date 2021/6/24
 */
public class ThriftClientException extends RuntimeException {
    public ThriftClientException(String message) {
        super(message);
    }

    public ThriftClientException(String message, Throwable t) {
        super(message, t);
    }

}
