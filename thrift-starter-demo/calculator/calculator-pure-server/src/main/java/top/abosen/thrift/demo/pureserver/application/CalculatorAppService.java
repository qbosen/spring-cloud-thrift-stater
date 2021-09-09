package top.abosen.thrift.demo.pureserver.application;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */
@Service
public class CalculatorAppService {
    public int add(int arg1, int arg2) {
        BigDecimal arg1Decimal = new BigDecimal(arg1);
        BigDecimal arg2Decimal = new BigDecimal(arg2);
        return arg1Decimal.add(arg2Decimal).intValue();
    }

    public int subtract(int arg1, int arg2) {
        BigDecimal arg1Decimal = new BigDecimal(arg1);
        BigDecimal arg2Decimal = new BigDecimal(arg2);
        return arg1Decimal.subtract(arg2Decimal).intValue();
    }

    public int multiply(int arg1, int arg2) {
        BigDecimal arg1Decimal = new BigDecimal(arg1);
        BigDecimal arg2Decimal = new BigDecimal(arg2);
        return arg1Decimal.multiply(arg2Decimal).intValue();
    }

    public int division(int arg1, int arg2) {
        BigDecimal arg1Decimal = new BigDecimal(arg1);
        BigDecimal arg2Decimal = new BigDecimal(arg2);
        return arg1Decimal.divide(arg2Decimal, BigDecimal.ROUND_CEILING).intValue();
    }
}
