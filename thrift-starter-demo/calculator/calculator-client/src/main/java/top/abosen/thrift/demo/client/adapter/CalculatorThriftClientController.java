package top.abosen.thrift.demo.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.abosen.thrift.demo.client.service.CalculatorThriftClient;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */

@RestController
@RequestMapping("/thrift")
@RequiredArgsConstructor
public class CalculatorThriftClientController {
    final CalculatorThriftClient calculatorThriftClient;

    @GetMapping("/add")
    public int add(@RequestParam("arg1") int arg1, @RequestParam("arg2") int arg2) {
        return calculatorThriftClient.add(arg1, arg2);
    }

    @GetMapping("/subtract")
    public int subtract(@RequestParam("arg1") int arg1, @RequestParam("arg2") int arg2) {
        return calculatorThriftClient.subtract(arg1, arg2);
    }

    @GetMapping("/multiply")
    public int multiply(@RequestParam("arg1") int arg1, @RequestParam("arg2") int arg2) {
        return calculatorThriftClient.multiply(arg1, arg2);
    }

    @GetMapping("/division")
    public int division(@RequestParam("arg1") int arg1, @RequestParam("arg2") int arg2) {
        return calculatorThriftClient.division(arg1, arg2);
    }
}
