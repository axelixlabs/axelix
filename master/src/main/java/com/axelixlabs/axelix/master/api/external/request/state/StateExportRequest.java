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
package com.axelixlabs.axelix.master.api.external.request.state;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jspecify.annotations.NonNull;

import com.axelixlabs.axelix.master.api.external.request.deserilize.StateExportComponentDeserializer;

/**
 * Request for export for of the state of the application.
 *
 * @param components List of components to export with their settings.
 * @author Mikhail Polivakha
 */
public record StateExportRequest(
        @NonNull @JsonDeserialize(using = StateExportComponentDeserializer.class)
        List<StateComponentSettings> components) {

    @Override
    public List<StateComponentSettings> components() {
        return components;
    }
}
