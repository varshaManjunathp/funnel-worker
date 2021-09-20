package com.pharmeasy.funnel.exception;

import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

@NoArgsConstructor
public class ExceptionUtils {

    public static Supplier<UnscheduledException> notFoundId(Class<?> clazz, Object id) {
        String msg = generateMessage(clazz, Collections.singletonMap("id", id.toString()));
        return () -> new UnscheduledException(msg);
    }

    private static String generateMessage(Class<?> clazz, Map<String, String> searchParams) {
        return clazz.getSimpleName() + " was not found for parameters " + searchParams;
    }
}
