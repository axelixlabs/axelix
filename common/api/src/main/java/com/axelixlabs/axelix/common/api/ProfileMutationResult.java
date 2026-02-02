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
package com.axelixlabs.axelix.common.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The response to profile-management actuator endpoint.
 *
 * @since 24.09.2025
 * @author Nikita Kirillov
 */
public final class ProfileMutationResult {

    private final boolean updated;
    private final String reason;

    @JsonCreator
    public ProfileMutationResult(@JsonProperty("updated") boolean updated, @JsonProperty("reason") String reason) {
        this.updated = updated;
        this.reason = reason;
    }

    public boolean updated() {
        return updated;
    }

    public String reason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProfileMutationResult that = (ProfileMutationResult) o;
        return updated == that.updated && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(updated, reason);
    }

    @Override
    public String toString() {
        return "ProfileMutationResult{" + "updated=" + updated + ", reason='" + reason + '\'' + '}';
    }
}
