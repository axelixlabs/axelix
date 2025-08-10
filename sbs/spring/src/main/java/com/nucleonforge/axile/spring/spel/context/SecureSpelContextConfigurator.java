package com.nucleonforge.axile.spring.spel.context;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationContext;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * This utility class creates pre-configured EvaluationContext instances with security restrictions
 * to prevent dangerous SpEL expressions from being executed.
 *
 * @see org.springframework.expression.EvaluationContext
 * @see org.springframework.expression.spel.support.StandardEvaluationContext
 *
 * @since 14.08.2025
 * @author Nikita Kirillov
 */
public final class SecureSpelContextConfigurator {

    private SecureSpelContextConfigurator() {}

    public static void configure(
            Set<String> allowedBeans,
            Set<String> allowedClasses,
            ApplicationContext applicationContext,
            StandardEvaluationContext evaluationContext) {

        evaluationContext.setRootObject(null);
        evaluationContext.setBeanResolver(new RestrictedBeanResolver(applicationContext, allowedBeans));
        evaluationContext.setTypeLocator(createTypeLocator(allowedClasses));
        evaluationContext.setPropertyAccessors(List.of(createPropertyAccessor(allowedClasses)));
        evaluationContext.setMethodResolvers(List.of(createMethodResolver()));
        evaluationContext.setConstructorResolvers(Collections.emptyList());
    }

    /**
     * Creates a restrictive TypeLocator that only allows specified classes.
     * <p>Modeled after Spring's StandardTypeLocator with additional security constraints.
     *
     * @return configured TypeLocator instance
     * @see org.springframework.expression.spel.support.StandardTypeLocator
     */
    private static TypeLocator createTypeLocator(Set<String> allowedClasses) {
        return typeName -> {
            Assert.notNull(typeName, "Type name must not be null");

            if (!allowedClasses.contains(typeName)) {
                throw new SpelEvaluationException(
                        SpelMessage.TYPE_NOT_FOUND, "Class [" + typeName + "] is not allowed for SpEL expressions");
            }

            try {
                return ClassUtils.forName(typeName, ClassUtils.getDefaultClassLoader());
            } catch (ClassNotFoundException | LinkageError ex) {
                throw new SpelEvaluationException(
                        SpelMessage.TYPE_NOT_FOUND, "Could not load class [" + typeName + "]", ex);
            }
        };
    }

    private static PropertyAccessor createPropertyAccessor(Set<String> allowedClasses) {
        return new ReflectivePropertyAccessor() {

            @Override
            public boolean canRead(@NonNull EvaluationContext ctx, @Nullable Object target, @NonNull String name)
                    throws AccessException {
                if ("class".equals(name)) {
                    throw new AccessException("Class access blocked");
                }

                if (target != null && !allowedClasses.contains(target.getClass().getName())) {
                    throw new AccessException(
                            "Access to class " + target.getClass().getName() + " blocked");
                }

                return super.canRead(ctx, target, name);
            }
        };
    }

    private static MethodResolver createMethodResolver() {
        return (ctx, target, name, args) -> {
            if (isDangerousMethod(target, name)) {
                throw new AccessException("Method " + name + " is blocked");
            }
            return new ReflectiveMethodResolver().resolve(ctx, target, name, args);
        };
    }

    private static boolean isDangerousMethod(Object target, String method) {
        return Set.of("getClass", "forName", "invoke", "exit").contains(method)
                || (target instanceof Class<?> clazz && isForbiddenClass(clazz));
    }

    private static boolean isForbiddenClass(Class<?> clazz) {
        String name = clazz.getName();
        return name.startsWith("java.lang.reflect")
                || name.startsWith("java.lang.invoke")
                || name.matches("java\\.lang\\.(System|Runtime|Process)");
    }
}
