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

import org.springframework.stereotype.Component;

/**
 * {@link ResourceMessageDeserializationStrategy} for heapdump.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@Component
public class HeapDumpMessageDeserializationStrategy extends ResourceMessageDeserializationStrategy {

    /**
     * This filename extension is valid only for HotSpot heapdump format.
     * <p>
     * Although, in 99% of cases hprof is the format of the actual binary
     * deserialized file, it might still be that someone is using OpenJ9 for
     * instance or anything, and head dump format would differ from hprof.
     */
    @Override
    protected String filename() {
        return "heapdump.hprof";
    }
}
