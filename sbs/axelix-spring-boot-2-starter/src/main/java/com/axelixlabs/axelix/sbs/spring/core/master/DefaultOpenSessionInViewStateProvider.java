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
package com.axelixlabs.axelix.sbs.spring.core.master;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.ClassUtils;

/**
 * Default {@link OpenSessionInViewStateProvider} based on Spring OSIV infrastructure beans.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public class DefaultOpenSessionInViewStateProvider implements OpenSessionInViewStateProvider {

    private static final ClassLoader CLASS_LOADER = DefaultOpenSessionInViewStateProvider.class.getClassLoader();

    private static final String OPEN_ENTITY_MANAGER_IN_VIEW_FILTER_CLASS_NAME =
        "org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter";

    private static final String OPEN_ENTITY_MANAGER_IN_VIEW_INTERCEPTOR_CLASS_NAME =
        "org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor";

    private final ListableBeanFactory beanFactory;

    public DefaultOpenSessionInViewStateProvider(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public boolean isOpenSessionInViewEnabled() {
        return hasOSIVFilter() || hasOSIVInterceptor();
    }

    private boolean hasOSIVInterceptor() {
        if (!ClassUtils.isPresent(OPEN_ENTITY_MANAGER_IN_VIEW_INTERCEPTOR_CLASS_NAME, CLASS_LOADER)) {
            return false;
        }
        Class<?> beanClass = ClassUtils.resolveClassName(OPEN_ENTITY_MANAGER_IN_VIEW_INTERCEPTOR_CLASS_NAME, CLASS_LOADER);
        return beanFactory.getBeanNamesForType(beanClass, false, false).length > 0;
    }

    private boolean hasOSIVFilter() {
        if (!ClassUtils.isPresent(OPEN_ENTITY_MANAGER_IN_VIEW_FILTER_CLASS_NAME, CLASS_LOADER)) {
            return false;
        }
        Class<?> beanClass = ClassUtils.resolveClassName(OPEN_ENTITY_MANAGER_IN_VIEW_FILTER_CLASS_NAME, CLASS_LOADER);
        return beanFactory.getBeanNamesForType(beanClass, false, false).length > 0;
    }
}
