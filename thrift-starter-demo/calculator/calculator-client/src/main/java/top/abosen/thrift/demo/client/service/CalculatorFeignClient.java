package top.abosen.thrift.demo.client.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author qiubaisen
 * @date 2021/6/25
 */

@FeignClient(name = "calculator-rest-server", path = "/rest")
public interface CalculatorFeignClient {
    @GetMapping("/add")
    int add(@RequestParam("arg1") int arg1, @RequestParam("arg2") int arg2);

    @GetMapping("/subtract")
    int subtract(@RequestParam("arg1") int arg1, @RequestParam("arg2") int arg2);

    @GetMapping("/multiply")
    int multiply(@RequestParam("arg1") int arg1, @RequestParam("arg2") int arg2);

    @GetMapping("/division")
    int division(@RequestParam("arg1") int arg1, @RequestParam("arg2") int arg2);

}
