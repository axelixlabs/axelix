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
package com.nucleonforge.axelix.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axelix.common.api.ThreadDumpFeed;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ThreadDumpFeed}.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
@Component
public class ThreadDumpJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ThreadDumpFeed> {

    public ThreadDumpJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ThreadDumpFeed> supported() {
        return ThreadDumpFeed.class;
    }
}
