package top.abosen.thrift.demo.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import top.abosen.thrift.client.annotation.EnableThriftClient;

/**
 * @author qiubaisen
 * @date 2021/6/25
 */


@SpringBootApplication
@EnableThriftClient
@EnableDiscoveryClient
@EnableFeignClients
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
