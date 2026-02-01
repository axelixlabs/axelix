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
package com.axelixlabs.axelix.master.service.export;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.domain.InstanceId;

/**
 * Default implementation of {@link StateArchiveFileNameGenerator}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class DefaultStateArchiveFileNameGenerator implements StateArchiveFileNameGenerator {

    public static final String STATE_ARCHIVE_FILE_TEMPLATE = "instance-state-%s-%s.zip";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'mm-HH-ss");

    @Override
    public String generate(InstanceId instanceId) {
        return STATE_ARCHIVE_FILE_TEMPLATE.formatted(
                instanceId.instanceId(), FORMATTER.format(Instant.now().atZone(ZoneOffset.systemDefault())));
    }
}
