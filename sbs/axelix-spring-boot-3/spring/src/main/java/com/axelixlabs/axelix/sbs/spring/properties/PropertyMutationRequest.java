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
package com.axelixlabs.axelix.sbs.spring.properties;

/**
 * Represents a request to update (mutate) a specific configuration property
 * in the application.
 *
 * @param propertyName the name of the property to update. Must not be {@code null}.
 * @param newValue the new value to assign to the property.  May be {@code null} or empty.
 *
 * @since 26.09.2025
 * @author Nikita Kirillov
 */
public record PropertyMutationRequest(String propertyName, String newValue) {}
