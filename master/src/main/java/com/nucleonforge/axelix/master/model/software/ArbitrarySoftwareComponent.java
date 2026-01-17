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
package com.nucleonforge.axelix.master.model.software;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Any arbitrary {@link SoftwareComponent versionable component} that is used in the app, like
 * any specific library e.g. jackson, vavr, rabbitmq-client etc.
 *
 * @author Mikhail Polivakha
 */
public record ArbitrarySoftwareComponent(String name, String description, boolean isCore) implements SoftwareComponent {

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public boolean isCore() {
        return isCore;
    }
}
