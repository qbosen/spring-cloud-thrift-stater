package top.abosen.thrift.server.properties;

/**
 * todo configuration-processor 自动提示
 *
 * @author qiubaisen
 * @date 2021/6/15
 */
public final class ServerMode {
    /**
     * 服务模型 - 单线程阻塞式
     */
    public static final String SIMPLE = "simple";

    /**
     * 服务模型 - 单线程非阻塞式
     */
    public static final String NON_BLOCKING = "nonBlocking";

    /**
     * 服务模型 - 线程池
     */
    public static final String THREAD_POOL = "threadPool";

    /**
     * 服务模型 - 半同步半异步
     */
    public static final String HS_HA = "hsHa";

    /**
     * 服务模型 - 线程池选择器
     */
    public static final String THREADED_SELECTOR = "threadedSelector";

    /**
     * 默认的服务模型
     */
    public static final String DEFAULT = HS_HA;

    public static final String[] ALL_MODE = {SIMPLE, NON_BLOCKING, THREAD_POOL, HS_HA, THREADED_SELECTOR};
}
