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

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.contract.metadatav2.MetadataV2;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link MetadataV2}.
 *
 * @author Nikita Kirillov
 */
@Component
public class MetadataV2JacksonMessageDeserializationStrategy extends JacksonMessageDeserializationStrategy<MetadataV2> {

    public MetadataV2JacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<MetadataV2> supported() {
        return MetadataV2.class;
    }
}
