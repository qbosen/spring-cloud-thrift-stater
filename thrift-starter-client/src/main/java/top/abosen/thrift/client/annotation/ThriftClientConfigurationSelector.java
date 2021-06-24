package top.abosen.thrift.client.annotation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.env.Environment;
import top.abosen.thrift.common.Constants;
import top.abosen.thrift.common.ServiceMode;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */

@Slf4j
public class ThriftClientConfigurationSelector extends SpringFactoryImportSelector<EnableThriftClient> {
    @Override
    protected boolean isEnabled() {
        return true;
    }
}
