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

import org.springframework.core.env.AbstractEnvironment;

/**
 * A simplified version analogous to Spring's org.springframework.mock.env.MockEnvironment.
 * <p>
 * Used as a lightweight alternative to {@code StandardEnvironment} specifically
 * for isolated property validation tasks.
 *
 * @see MockPropertySource
 * @author Nikita Kirillov
 */
public class MockEnvironment extends AbstractEnvironment {

    private final MockPropertySource propertySource = new MockPropertySource();

    public MockEnvironment() {
        this.getPropertySources().addFirst(propertySource);
    }

    public void addProperty(String key, Object value) {
        this.propertySource.setProperty(key, value);
    }
}
