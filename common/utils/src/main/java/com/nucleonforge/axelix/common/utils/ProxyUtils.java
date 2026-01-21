/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.common.utils;

import java.lang.reflect.Proxy;

import org.springframework.util.ClassUtils;

import static com.nucleonforge.axelix.common.api.BeansFeed.ProxyType;

/**
 * Allows detection and analysis of proxies in Spring Boot.
 *
 * @author Sergey Cherkasov
 */
public class ProxyUtils {

    /**
     * Determines the type of proxy used by the given bean class.
     *
     * @param beanType   the bean class at runtime.
     * @return the proxy type, or {@link ProxyType#NO_PROXYING} if the class is not proxied.
     */
    public static ProxyType analyzeProxyType(Class<?> beanType) {
        if (Proxy.isProxyClass(beanType)) {
            return ProxyType.JDK_PROXY;
        } else if (beanType.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR) && !beanType.isHidden()) {
            return ProxyType.CGLIB;
        }

        return ProxyType.NO_PROXYING;
    }

    /**
     * Resolves the actual user class for the given bean class.
     *
     * @param beanClass   the bean class at runtime.
     * @return the proxy interface if the class is a proxy, or the original user class otherwise.
     */
    public static Class<?> resolveUserClass(Class<?> beanClass) {
        Class<?> userClass = ClassUtils.getUserClass(beanClass);

        if (userClass == beanClass && Proxy.isProxyClass(userClass)) {
            userClass = userClass.getInterfaces()[0];
        }

        return userClass;
    }
}
