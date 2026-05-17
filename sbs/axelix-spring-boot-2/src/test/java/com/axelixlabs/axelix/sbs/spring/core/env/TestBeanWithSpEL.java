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
package com.axelixlabs.axelix.sbs.spring.core.env;

import org.springframework.beans.factory.annotation.Value;

/**
 * Test fixture for {@link ValueInjectionTrackerBeanPostProcessor}. Exercises SpEL-based
 * value injection patterns ({@code environment.getProperty(...)} and
 * {@code systemProperties[...]}) that the tracker recognizes alongside the regular
 * {@code ${...}} placeholder form.
 *
 * @since 16.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class TestBeanWithSpEL {

    @Value("#{environment.getProperty('server.port')}")
    private String envPort;

    @Value("#{systemProperties['user.home']}")
    private String systemHome;

    @Value("#{environment.getProperty('app.timeout')}")
    public Integer getTimeout() {
        return 5000;
    }
}
