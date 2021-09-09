package top.abosen.thrift.server.properties;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author qiubaisen
 * @date 2021/9/7
 */
public class ThriftServerConfigureWrapper {

    private final Map<String, ThriftServerConfigure> configureMap;

    public ThriftServerConfigureWrapper(List<ThriftServerConfigure> configures) {
        configureMap = configures.stream().collect(Collectors.toMap(ThriftServerConfigure::configureName, Function.identity()));
    }

    public ThriftServerConfigure getConfigure(String configureName){
        return configureMap.get(configureName);
    }
}
