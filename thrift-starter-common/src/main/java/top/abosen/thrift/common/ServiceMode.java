package top.abosen.thrift.common;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */
public enum ServiceMode {
    /**
     * 服务模型 - 单线程阻塞式
     */
    SIMPLE,
    /**
     * 服务模型 - 单线程非阻塞式
     */
    NON_BLOCKING,
    /**
     * 服务模型 - 线程池
     */
    THREAD_POOL,
    /**
     * 服务模型 - 半同步半异步
     */
    HS_HA,

    /**
     * 服务模型 - 线程池选择器
     */
    THREADED_SELECTOR,
    ;

    /**
     * 默认的服务模型
     */
    public static final ServiceMode DEFAULT = THREADED_SELECTOR;

}
