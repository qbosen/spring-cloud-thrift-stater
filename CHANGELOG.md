# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.11]

### Changed

- 支持无 Spring Cloud 的服务端启动: `spring.cloud.consul.enabled=false`
- 客户端 遇到服务端未定义异常时, 不再清理对应 transport 连接池, 避免频繁的连接创建和销毁

## [1.0.10]

### Added

- `hsha/threaded_selector` 增加 `backlog` 配置, 避免连接过多时, 服务端拒绝/重置连接
- 增加 `ThriftThreadFactory`; 所有thrift工作线程按 `thrift-[server]-thread-%d` 命名

## [1.0.9]

### Changed

- ThriftServer 支持 Spring AOP 切面

## [1.0.8]

### Changed

- 增加客户端调用时的网络错误日志
- ThriftClientContext 增加beanName常量, 用于bean依赖排序
    - `@DependsOn(ThriftClientContext.BEAN_NAME)` 可以在bean初始化过程中提前进行thrift调用

## [1.0.7]

### Added

- 客户端调试工具支持更多参数

## [1.0.6]

### Fixed

- 服务端增加 `maxReadBufferBytes` 配置, 避免协议错误的数据包解析过程OOM, 默认1M

## [1.0.5]

### Added

- 服务端支持全局异常转换
- 客户端增加调用测试工具

## [1.0.4]

### Added

- 客户端增加端口配置器
  > 配置客户端端口范围,避免抢占其他服务的端口

## [1.0.3]

### Added

- 增加 api2thrift configure
  > 用于客户端根据服务端注册的api服务地址, 按照约定发现thrift服务地址
  > 约定: 兼容模式thrift服务端口为 api端口+1, 标准模式端口为 api端口+2

## [1.0.2]

### Changed

- 服务端捕获异常
- 客户端业务异常不重试

## [1.0.1]

### Added

- 自定义客户端configure
- 自定义服务端configure
