/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.utils;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Optional;

import org.springframework.aop.Advisor;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * Decides how (or whether) a bean can be safely proxied, and builds that proxy.
 *
 * @author Nikita Kirillov
 */
public final class ProxyingUtils {

    private ProxyingUtils() {}

    private enum ProxyStrategy {
        /** Target is safe to subclass with CGLIB - either a plain class, or an unrelated, non-Spring proxy. */
        CGLIB,
        /** Target is already a JDK dynamic proxy - proxy it again via its own interfaces, no CGLIB involved. */
        JDK_DYNAMIC,
        /** Target is already Advised and not frozen - mutate it directly to avoid a permanent extra delegation hop. */
        ADD_ADVISOR,
        /** Target cannot be proxied safely by either mechanism. */
        UNSAFE
    }

    /**
     * Advises {@code target} with {@code advisor}, choosing the safest way to do so.
     *
     * @return the advised object, or {@link Optional#empty()} if {@code target} cannot be proxied safely.
     */
    public static Optional<Object> tryCreateProxy(Object target, Advisor advisor) {
        ProxyStrategy strategy = determineProxyStrategy(target, advisor);

        if (strategy == ProxyStrategy.UNSAFE) {
            return Optional.empty();
        }

        if (strategy == ProxyStrategy.ADD_ADVISOR) {
            // Index 0 runs first, same as wrapping target in a new outer proxy would.
            ((Advised) target).addAdvisor(0, advisor);
            return Optional.of(target);
        }

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(target);

        if (strategy == ProxyStrategy.JDK_DYNAMIC) {
            Class<?>[] userInterfaces = AopProxyUtils.proxiedUserInterfaces(target);
            proxyFactory.setInterfaces(userInterfaces);
        } else {
            proxyFactory.setProxyTargetClass(true);
        }

        proxyFactory.addAdvisor(advisor);
        return Optional.of(proxyFactory.getProxy());
    }

    private static ProxyStrategy determineProxyStrategy(Object target, Advisor advisor) {
        // IntroductionAdvisor - the only way to add an interface via AOP - can't be retrofitted onto an
        // already-built proxy, so it always needs a freshly built one.
        if (target instanceof Advised && !((Advised) target).isFrozen() && !(advisor instanceof IntroductionAdvisor)) {
            return ProxyStrategy.ADD_ADVISOR;
        }

        Class<?> targetClass = target.getClass();

        if (Proxy.isProxyClass(targetClass)) {
            return ProxyStrategy.JDK_DYNAMIC;
        }
        // targetClass could be a non-Spring CGLIB proxy here - checking the user class instead avoids
        // false positives from final methods the CGLIB-generated class adds on its own.
        Class<?> userClass = ClassUtils.getUserClass(targetClass);
        if (isSafeToCGLIBProxy(userClass)) {
            return ProxyStrategy.CGLIB;
        }

        return ProxyStrategy.UNSAFE;
    }

    private static boolean isSafeToCGLIBProxy(Class<?> targetClass) {
        return !Modifier.isFinal(targetClass.getModifiers()) && !hasFinalProxyableMethod(targetClass);
    }

    private static boolean hasFinalProxyableMethod(Class<?> targetClass) {
        MethodFilter finalMethodFilter = method -> !ReflectionUtils.isObjectMethod(method)
                && !Modifier.isPrivate(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers())
                && Modifier.isFinal(method.getModifiers());

        return ReflectionUtils.getUniqueDeclaredMethods(targetClass, finalMethodFilter).length > 0;
    }
}
