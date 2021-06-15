package top.abosen.thrift.server.utils;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author qiubaisen
 * @date 2021/6/15
 */
@UtilityClass
public class ClassUtils {
    public static Optional<Class<?>> findFirstInterface(Class<?> clazz, Predicate<Class<?>> predicate) {
        Class<?> current = clazz;
        while (current != null) {
            for (Class<?> ifc : current.getInterfaces()) {
                if (predicate.test(ifc)) {
                    return Optional.of(ifc);
                }
            }
            current = current.getSuperclass();
        }
        return Optional.empty();
    }

}
