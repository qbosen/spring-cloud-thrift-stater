package top.abosen.thrift.server.exception;

/**
 * @author qiubaisen
 * @date 2021/6/17
 */
public class ThriftServerException extends RuntimeException{
    public ThriftServerException(String message) {
        super(message);
    }

    public ThriftServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
