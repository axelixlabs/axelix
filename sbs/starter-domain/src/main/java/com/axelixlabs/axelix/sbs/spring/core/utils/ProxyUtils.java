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

import java.lang.reflect.Proxy;

import com.axelixlabs.axelix.common.api.BeansFeed;

/**
 * Utilities to work with proxy.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class ProxyUtils {
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

    /**
     * Determines the type of proxy used by the given bean class.
     *
     * @param isRuntimeGeneratedClass   whether the class is runtime-generated (hidden or synthetic)
     * @param beanType                  the bean class at runtime.
     * @return the proxy type, or ProxyType#NO_PROXYING if the class is not proxied.
     */
    public static BeansFeed.ProxyType analyzeProxyType(Class<?> beanType, boolean isRuntimeGeneratedClass) {
        if (Proxy.isProxyClass(beanType)) {
            return BeansFeed.ProxyType.JDK_PROXY;
        } else if (beanType.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR) && !isRuntimeGeneratedClass) {
            return BeansFeed.ProxyType.CGLIB;
        }
        return BeansFeed.ProxyType.NO_PROXYING;
    }
}
