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
package com.axelixlabs.axelix.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.api.gclog.GcLogStatusResponse;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link GcLogStatusResponse}.
 *
 * @since 11.01.2026
 * @author Nikita Kirillov
 */
@Component
public class GcLogStatusMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<GcLogStatusResponse> {

    public GcLogStatusMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<GcLogStatusResponse> supported() {
        return GcLogStatusResponse.class;
    }
}
