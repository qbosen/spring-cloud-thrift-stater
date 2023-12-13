package top.abosen.thrift.demo.pureserver;

import lombok.SneakyThrows;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ExceptionUtils;
import top.abosen.thrift.client.ThriftClientTestUtil;
import top.abosen.thrift.demo.calculator.thrift.CalculatorService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * @author qiubaisen
 * @since 2023/12/13
 */

class ApplicationTest {

    @Test
    void test_normal() {
        test_action(50, client -> client.add(100, 0));

    }

    @Test
    void test_TAppException() {
        test_action(100, client -> client.division(100, 0));
    }

    interface Invocation {
        void invoke(CalculatorService.Iface iface) throws TException;
    }

    @SneakyThrows
    private static void test_action(int times, Invocation invocation) {

        CompletableFuture.allOf(IntStream.range(0, times)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        CalculatorService.Iface client = ThriftClientTestUtil.getClientByIface(CalculatorService.Iface.class, "calculator-thrift-server", "localhost", 8081);
                        invocation.invoke(client);
                    } catch (UndeclaredThrowableException e) {
                        Throwable undeclaredThrowable = e.getUndeclaredThrowable();
                        if (undeclaredThrowable instanceof InvocationTargetException) {
                            System.out.println(((InvocationTargetException) undeclaredThrowable).getTargetException().getMessage());
                        } else {
                            System.out.println(undeclaredThrowable.getMessage());

                        }
                    } catch (Exception e) {
                        ExceptionUtils.throwAsUncheckedException(e);
                    }

                    System.out.println("Task " + i + " completed.");
                }))
                .toArray(CompletableFuture[]::new)).join();
    }
}