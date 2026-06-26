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
package com.axelixlabs.axelix.master.domain;

/**
 * The id of the application that a given {@link Instance} belongs to. It is composed of the {@code groupId} and the
 * {@code artifactId} (the G and A inside the GAV coordinate) and is shared by all the instances of the same
 * application. This id is mandatory - an {@link Instance} cannot be registered without a valid {@link ApplicationId}.
 *
 * @param groupId    the group id of the application artifact (the G inside the GAV coordinate)
 * @param artifactId the artifact id of the application artifact (the A inside the GAV coordinate)
 * @author Mikhail Polivakha
 */
public record ApplicationId(String groupId, String artifactId) {

    public static ApplicationId of(String groupId, String artifactId) {
        return new ApplicationId(groupId, artifactId);
    }
}
