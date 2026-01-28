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
package com.axelixlabs.axelix.sbs.spring.conditions;

import java.lang.reflect.Proxy;

import org.jspecify.annotations.Nullable;

import org.springframework.util.ClassUtils;

/**
 * Default implementation {@link ConditionalBeanRefBuilder}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public class DefaultConditionalBeanRefBuilder implements ConditionalBeanRefBuilder {

    public String buildBeanRefInternal(Class<?> beanClass, @Nullable String beanFactoryMethodName) {

        Class<?> userClass = resolveUserClass(beanClass);

        // By calling this method spring-boot replaces the nested class '$' with package separator - the dot.
        // https://github.com/spring-projects/spring-boot/blob/main/module/spring-boot-actuator-autoconfigure/src/main/java/org/springframework/boot/actuate/autoconfigure/condition/ConditionsReportEndpoint.java#L128
        var result = new StringBuilder(ClassUtils.getShortName(userClass));

        if (beanFactoryMethodName != null) {
            // We append the method via '#' sign in order to cope with the following behavior
            // https://github.com/spring-projects/spring-boot/blob/main/core/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/condition/SpringBootCondition.java#L79
            result.append("#").append(beanFactoryMethodName);
        }

        return result.toString();
    }

    // TODO:
    //  We have this logic  of determining the end user class spread
    //  across the project, especially in beans endpoint. Need to unify it.
    private static Class<?> resolveUserClass(Class<?> beanClass) {
        Class<?> userClass = ClassUtils.getUserClass(beanClass);

        if (userClass == beanClass && Proxy.isProxyClass(userClass)) {
            userClass = userClass.getInterfaces()[0];
        }
        return userClass;
    }
}
