package top.abosen.thrift.demo.client.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import top.abosen.thrift.demo.calculator.thrift.CalculatorService;

/**
 * @author qiubaisen
 * @date 2021/6/25
 */
@Service
@RequiredArgsConstructor
public class CalculatorThriftClient {
    final CalculatorService.Iface iface;

    @SneakyThrows
    public int add(int arg1, int arg2) {
        return iface.add(arg1, arg2);
    }

    @SneakyThrows
    public int subtract(int arg1, int arg2) {
        return iface.subtract(arg1, arg2);
    }

    @SneakyThrows
    public int multiply(int arg1, int arg2) {
        return iface.multiply(arg1, arg2);
    }

    @SneakyThrows
    public int division(int arg1, int arg2) {
        return iface.division(arg1, arg2);
    }
}
