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

import java.io.Closeable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ProxyingUtils}.
 *
 * @author Nikita Kirillov
 */
class ProxyingUtilsTest {

    @Test
    void tryCreateProxy_shouldCreateCglibProxyForPlainTarget() {
        Plain target = new Plain();
        AtomicInteger calls = new AtomicInteger();

        Optional<Object> result = ProxyingUtils.tryCreateProxy(target, recordingAdvisor(calls));

        assertThat(result).isPresent();
        assertThat(AopUtils.isCglibProxy(result.get())).isTrue();

        ((Plain) result.get()).greet();
        assertThat(calls).hasValue(1);
    }

    @Test
    void tryCreateProxy_shouldReturnEmptyForFinalClass() {
        Optional<Object> result =
                ProxyingUtils.tryCreateProxy(new FinalTarget(), recordingAdvisor(new AtomicInteger()));

        assertThat(result).isEmpty();
    }

    @Test
    void tryCreateProxy_shouldReturnEmptyForClassWithFinalMethod() {
        Optional<Object> result =
                ProxyingUtils.tryCreateProxy(new FinalMethodTarget(), recordingAdvisor(new AtomicInteger()));

        assertThat(result).isEmpty();
    }

    @Test
    void tryCreateProxy_shouldWrapExistingJdkProxyViaItsOwnInterfaces() {
        Plain target = new Plain();
        Greeter jdkProxy = (Greeter) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] {Greeter.class, Closeable.class},
                (proxy, method, args) -> method.invoke(target, args));

        AtomicInteger calls = new AtomicInteger();
        Optional<Object> result = ProxyingUtils.tryCreateProxy(jdkProxy, recordingAdvisor(calls));

        assertThat(result).isPresent();
        assertThat(AopUtils.isJdkDynamicProxy(result.get())).isTrue();
        assertThat(result.get()).isInstanceOf(Closeable.class);

        ((Greeter) result.get()).greet();
        assertThat(calls).hasValue(1);
    }

    @Test
    void tryCreateProxy_shouldAddAdvisorToExistingCglibProxyInsteadOfWrappingAgain() {
        Plain target = new Plain();
        List<String> callOrder = new ArrayList<>();

        Object firstLayer = ProxyingUtils.tryCreateProxy(target, orderingAdvisor(callOrder, "first"))
                .orElseThrow();
        assertThat(AopUtils.isCglibProxy(firstLayer)).isTrue();

        Optional<Object> result = ProxyingUtils.tryCreateProxy(firstLayer, orderingAdvisor(callOrder, "second"));

        assertThat(result).isPresent();
        // No new proxy layer - the advisor was added directly to the existing one.
        assertThat(result.get()).isSameAs(firstLayer);

        ((Plain) result.get()).greet();

        // The newly added advisor runs first and wraps the existing one, same as an outer proxy would.
        assertThat(callOrder).containsExactly("second-before", "first-before", "first-after", "second-after");
    }

    @Test
    void tryCreateProxy_shouldAddAdvisorToExistingJdkProxyInsteadOfWrappingAgain() {
        Plain target = new Plain();
        List<String> callOrder = new ArrayList<>();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(target);
        proxyFactory.setInterfaces(Greeter.class);
        proxyFactory.addAdvisor(orderingAdvisor(callOrder, "first"));
        Object existingJdkProxy = proxyFactory.getProxy();
        assertThat(AopUtils.isJdkDynamicProxy(existingJdkProxy)).isTrue();

        Optional<Object> result = ProxyingUtils.tryCreateProxy(existingJdkProxy, orderingAdvisor(callOrder, "second"));

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(existingJdkProxy);

        ((Greeter) result.get()).greet();

        assertThat(callOrder).containsExactly("second-before", "first-before", "first-after", "second-after");
    }

    // This is an IMPORTANT test; if you need to modify or delete it, pls ensure you understand what you are doing.
    @Test
    void tryCreateProxy_shouldBuildNewProxyForIntroductionAdvisorEvenIfTargetAlreadyAdvised() {
        Plain target = new Plain();
        Object existingProxy = ProxyingUtils.tryCreateProxy(target, recordingAdvisor(new AtomicInteger()))
                .orElseThrow();
        assertThat(existingProxy).isNotInstanceOf(Extra.class);

        Advisor introductionAdvisor = new DefaultIntroductionAdvisor(new ExtraIntroduction(), Extra.class);
        Optional<Object> result = ProxyingUtils.tryCreateProxy(existingProxy, introductionAdvisor);

        assertThat(result).isPresent();
        // A fresh proxy, not the mutated original - introduced interfaces can't be retrofitted in place.
        assertThat(result.get()).isNotSameAs(existingProxy);
        assertThat(result.get()).isInstanceOf(Extra.class);
        assertThat(((Extra) result.get()).extra()).isEqualTo("extra");
    }

    private static Advisor recordingAdvisor(AtomicInteger calls) {
        MethodInterceptor interceptor = invocation -> {
            calls.incrementAndGet();
            return invocation.proceed();
        };
        return new DefaultPointcutAdvisor(interceptor);
    }

    private static Advisor orderingAdvisor(List<String> callOrder, String label) {
        MethodInterceptor interceptor = invocation -> {
            callOrder.add(label + "-before");
            try {
                return invocation.proceed();
            } finally {
                callOrder.add(label + "-after");
            }
        };
        return new DefaultPointcutAdvisor(interceptor);
    }

    interface Greeter {
        void greet();
    }

    static class Plain implements Greeter {
        @Override
        public void greet() {}
    }

    static final class FinalTarget {}

    static class FinalMethodTarget {
        public final void greet() {}
    }

    interface Extra {
        String extra();
    }

    static class ExtraIntroduction extends DelegatingIntroductionInterceptor implements Extra {
        @Override
        public String extra() {
            return "extra";
        }
    }
}
