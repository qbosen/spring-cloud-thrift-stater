# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
