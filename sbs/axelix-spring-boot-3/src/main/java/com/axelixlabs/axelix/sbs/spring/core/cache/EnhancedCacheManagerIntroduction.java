package com.axelixlabs.axelix.sbs.spring.core.cache;

import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;

/**
 * Routes {@link EnhancedCacheManager} (and its inherited {@link org.springframework.cache.CacheManager})
 * invocations on the proxy to the supplied delegate, while letting any concrete-class-specific method
 * proceed to the proxied target. This preserves the runtime type of the original {@code CacheManager}
 * bean while exposing the enhanced management API.
 */
public class EnhancedCacheManagerIntroduction implements IntroductionInterceptor {
    private final EnhancedCacheManager delegate;

    public EnhancedCacheManagerIntroduction(EnhancedCacheManager delegate) {
        this.delegate = delegate;
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method delegateMethod = findDelegateMethod(invocation.getMethod());

        if (delegateMethod != null)
            return AopUtils.invokeJoinpointUsingReflection(delegate, delegateMethod, invocation.getArguments());

        return invocation.proceed();
    }

    @Override
    public boolean implementsInterface(Class<?> intf) {
        return intf.isAssignableFrom(EnhancedCacheManager.class);
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
