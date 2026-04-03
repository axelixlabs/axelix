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

import java.util.Properties;

import org.springframework.core.env.PropertiesPropertySource;

/**
 * A simplified version analogous to Spring's org.springframework.mock.env.MockPropertySource.
 * <p>
 * Designed as a lightweight provider for property validation without the overhead
 * of full Spring environment infrastructure.
 *
 * @see MockEnvironment
 * @author Nikita Kirillov
 */
public class MockPropertySource extends PropertiesPropertySource {

    public static final String MOCK_PROPERTIES_PROPERTY_SOURCE_NAME = "mockProperties";

    public MockPropertySource() {
        super(MOCK_PROPERTIES_PROPERTY_SOURCE_NAME, new Properties());
    }

    public void setProperty(String name, Object value) {
        this.source.put(name, value);
    }
}
