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

import com.axelixlabs.axelix.common.api.BeansFeed.BeanMethod;
import com.axelixlabs.axelix.common.api.BeansFeed.BeanSource;

/**
 * Utilities to work with bean names.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public class BeanNameUtils {

    /**
     * Strips the configprops prefix from the bean name.
     * <p>
     * The problem is that the bean name of the configprops bean as returned by the actuator, for some reason, contains
     * the dash at the very beginning. I do not know why. We do not want to show it in the bean name.
     */
    public static String stripConfigPropsPrefix(String beanName) {
        int indexOfDash = beanName.indexOf("-");

        if (indexOfDash != -1 && indexOfDash < beanName.length() - 1) {
            return beanName.substring(indexOfDash + 1);
        } else {
            return beanName;
        }
    }

    /**
     * Returns the bean name without the configuration properties prefix if the bean
     * represents a {@code @ConfigurationProperties} bean.
     *
     * @param beanName          the original bean name.
     * @param isConfigPropsBean whether the bean is a configuration properties bean.
     * @return the bean name with the configuration properties prefix removed,
     * or the original name if the bean is not a configuration properties bean.
     */
    public static String withoutConfigPropsPrefix(String beanName, boolean isConfigPropsBean) {
        return isConfigPropsBean ? BeanNameUtils.stripConfigPropsPrefix(beanName) : beanName;
    }

    /**
     * Resolves the effective bean type name.
     *
     * @param clazz                     the actual bean class (potentially proxied or runtime-generated)
     * @param beanSource                the source describing how the bean was defined
     * @param isRuntimeGeneratedClass   whether the class is runtime-generated (hidden or synthetic)
     * @return the primary interface name for runtime-generated beans declared via {@link BeanMethod},
     * otherwise the resolved user class name
     */
    public static String resolveBeanTypeName(Class<?> clazz, BeanSource beanSource, boolean isRuntimeGeneratedClass) {

        if (isRuntimeGeneratedClass && beanSource instanceof BeanMethod && clazz.getInterfaces().length > 0) {
            return clazz.getInterfaces()[0].getName();
        }

        return ClassUtils.getUserClass(clazz).getName();
    }
}
