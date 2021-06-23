package top.abosen.thrift.common;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author qiubaisen
 * @date 2021/6/23
 */
@UtilityClass
public class Utils {
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

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object object) {
        return (T) object;
    }
}
