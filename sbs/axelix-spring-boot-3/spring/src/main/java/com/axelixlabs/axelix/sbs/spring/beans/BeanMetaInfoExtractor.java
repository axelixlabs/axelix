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
package com.axelixlabs.axelix.sbs.spring.beans;

import org.jspecify.annotations.NullMarked;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Interface capable to extract the {@link BeanMetaInfo} from the given {@link ConfigurableListableBeanFactory}.
 * by the given bean name.
 *
 * @since 04.07.2025
 * @author Nikita Kirillov
 */
@NullMarked
public interface BeanMetaInfoExtractor {

    /**
     * Enriches bean descriptor with additional analysis information.
     *
     * @param beanName    the name of the bean to analyze
     * @param beanFactory the bean factory that stores the bean with the given {@code beanName}
     * @return enriched bean information or empty if bean not found
     */
    BeanMetaInfo extract(String beanName, ConfigurableListableBeanFactory beanFactory);
}
