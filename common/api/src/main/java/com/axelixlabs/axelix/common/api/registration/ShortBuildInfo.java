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
package com.axelixlabs.axelix.common.api.registration;

/**
 * Short information about the build of the given service. Provided during initial scan.
 *
 * @param buildTimestamp the timestamp when this application's build was created
 * @param serviceVersion the version of the <strong>managed service itself</strong>, i.e. the version
 *                of the end-service artifact (the V inside GAV coordinate). The assumption is that
 *                is never {@code null}, and it frankly should not be.
 *
 * @author Mikhail Polivakha
 */
public record ShortBuildInfo(String buildTimestamp, String serviceVersion) {}
