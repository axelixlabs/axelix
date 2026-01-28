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

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.common.api.BeansFeed;

/**
 * Custom actuator endpoint that provides the beans feed.
 *
 * @since 08.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@RestControllerEndpoint(id = "axelix-beans")
public class AxelixBeansEndpoint {

    private final BeansFeedBuilder beansFeedBuilder;

    public AxelixBeansEndpoint(BeansFeedBuilder beansFeedBuilder) {
        this.beansFeedBuilder = beansFeedBuilder;
    }

    @GetMapping
    public BeansFeed beans() {
        return beansFeedBuilder.buildBeansFeed();
    }
}
