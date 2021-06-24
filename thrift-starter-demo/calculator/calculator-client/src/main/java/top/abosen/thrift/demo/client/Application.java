package top.abosen.thrift.demo.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import top.abosen.thrift.client.annotation.EnableThriftClient;

import javax.annotation.PostConstruct;

/**
 * @author qiubaisen
 * @date 2021/6/25
 */


@SpringBootApplication
@EnableThriftClient
public class Application {
    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    void init(){
        System.out.println();
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

}
