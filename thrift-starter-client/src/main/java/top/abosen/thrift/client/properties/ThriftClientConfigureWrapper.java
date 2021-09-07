package top.abosen.thrift.client.properties;

import top.abosen.thrift.common.Constants;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author qiubaisen
 * @date 2021/9/7
 */
public class ThriftClientConfigureWrapper {

    private final Map<String, ThriftClientConfigure> configureMap;

    public ThriftClientConfigureWrapper(List<ThriftClientConfigure> configures) {
        configureMap = configures.stream().collect(Collectors.toMap(ThriftClientConfigure::configureName, Function.identity()));
    }

    public ThriftClientConfigure getConfigure(String configureName){
        return configureMap.get(configureName);
    }
}
