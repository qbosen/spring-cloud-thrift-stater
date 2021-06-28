package top.abosen.thrift.demo.pureserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.abosen.thrift.server.annotation.EnableThriftServer;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */

@SpringBootApplication
@EnableThriftServer
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
