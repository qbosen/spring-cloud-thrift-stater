package top.abosen.thrift.server.exception;

/**
 * 服务端异常处理逻辑
 * <p>
 *     如果
 * </p>
 * @author qiubaisen
 * @date 2022/7/20
 */

@FunctionalInterface
public interface ThriftServerExceptionConverter {
    Throwable convert(Throwable exception);
}
