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
package com.axelixlabs.axelix.sbs.spring.core.cache;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.support.AopUtils;

/**
 * Routes {@link EnhancedCacheManager} (and its inherited {@link org.springframework.cache.CacheManager})
 * invocations on the proxy to the supplied delegate, while letting any concrete-class-specific method
 * proceed to the proxied target. This preserves the runtime type of the original {@code CacheManager}
 * bean while exposing the enhanced management API.
 */
class EnhancedCacheManagerIntroduction implements IntroductionInterceptor {

    private final EnhancedCacheManager delegate;

    EnhancedCacheManagerIntroduction(EnhancedCacheManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean implementsInterface(Class<?> intf) {
        return intf.isAssignableFrom(EnhancedCacheManager.class);
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method delegateMethod = findDelegateMethod(invocation.getMethod());

        if (delegateMethod != null)
            return AopUtils.invokeJoinpointUsingReflection(delegate, delegateMethod, invocation.getArguments());

        return invocation.proceed();
    }

    @Nullable
    private Method findDelegateMethod(Method invokedMethod) {
        try {
            return EnhancedCacheManager.class.getMethod(invokedMethod.getName(), invokedMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
