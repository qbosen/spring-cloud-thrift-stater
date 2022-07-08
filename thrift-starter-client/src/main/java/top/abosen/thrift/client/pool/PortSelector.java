package top.abosen.thrift.client.pool;

/**
 * @author qiubaisen
 * @date 2022/7/8
 */
public interface PortSelector {

    boolean isEnabled();

    int getMinPort();

    int getMaxPort();
}
