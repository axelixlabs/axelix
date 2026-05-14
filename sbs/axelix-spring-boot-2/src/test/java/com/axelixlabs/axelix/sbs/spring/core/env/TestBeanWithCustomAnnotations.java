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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Test fixture for {@link ValueInjectionTrackerBeanPostProcessor}. Exercises every
 * supported {@code @Value} placement (field, constructor parameter, method parameter,
 * method) using both direct {@code @Value} and the {@link TimeoutValue} meta-annotation.
 *
 * @since 16.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class TestBeanWithCustomAnnotations {

    @Value("${test.server.port:8080}")
    private String serverPort;

    @TimeoutValue
    private Integer timeout;

    public TestBeanWithCustomAnnotations(
            @Value("${test.spring.application.name:TestApp}") String appName, @TimeoutValue String connectionTimeout) {}

    private String profile;
    private Integer maxTimeout;

    @Autowired
    public void setProfile(@Value("${test.spring.profiles.active}") String profile) {
        this.profile = profile;
    }

    @Autowired
    public void setMaxTimeout(@TimeoutValue Integer timeout) {
        this.maxTimeout = timeout * 2;
    }

    @Value("${test.method.timeout}")
    public void calculateRandomTimeout() {}

    @TimeoutValue
    public void getDefaultTimeout() {}

    public String getServerPort() {
        return serverPort;
    }
}
