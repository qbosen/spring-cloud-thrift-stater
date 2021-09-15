package top.abosen.thrift.demo.server.adapter;

import lombok.RequiredArgsConstructor;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import top.abosen.thrift.demo.calculator.thrift.CalculatorService;
import top.abosen.thrift.demo.server.application.CalculatorAppService;
import top.abosen.thrift.server.annotation.ThriftService;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */

@ThriftService
@RequiredArgsConstructor
public class CalculatorThriftController implements CalculatorService.Iface {
    final CalculatorAppService calculatorAppService;

    @Override
    public int add(int arg1, int arg2) {
        return calculatorAppService.add(arg1, arg2);
    }

    @Override
    public int subtract(int arg1, int arg2) {
        return calculatorAppService.subtract(arg1, arg2);
    }

    @Override
    public int multiply(int arg1, int arg2) {
        return calculatorAppService.multiply(arg1, arg2);
    }

    @Override
    public int division(int arg1, int arg2) {
        return calculatorAppService.division(arg1, arg2);
    }
}
