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
package com.axelixlabs.axelix.master.autoconfiguration.auth.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;

/**
 * JWT configuration properties.
 *
 * @since 11.12.2025
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@SuppressWarnings("NullAway.Init")
@ConfigurationProperties(prefix = "axelix.master.auth.jwt")
public record JwtProperties(JwtAlgorithm algorithm, String signingKey, Duration lifespan) {}
