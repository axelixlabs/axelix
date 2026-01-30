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
package com.axelixlabs.axelix.sbs.spring.details;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for retrieving garbage collector information.
 *
 * @since 30.10.2025
 * @author Nikita Kirillov
 */
public class GarbageCollectorInfoAssembler {

    private static final Logger log = LoggerFactory.getLogger(GarbageCollectorInfoAssembler.class);

    private GarbageCollectorInfoAssembler() {}

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public static String getGarbageCollectorInfo() {
        try {
            List<String> gcNames = ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .map(GarbageCollectorMXBean::getName)
                    .collect(Collectors.toList());

            if (!gcNames.isEmpty()) {
                String joined = String.join(", ", gcNames).toLowerCase();

                GarageCollector garageCollector = GarageCollector.fromName(joined);

                if (garageCollector == GarageCollector.UNKNOWN) {
                    return String.join(", ", gcNames);
                }

                return garageCollector.name();
            }
        } catch (Exception exception) {
            log.warn(
                    "Unable to determine the GC used inside the given application. Falling back to UNKNOWN", exception);
        }

        return GarageCollector.UNKNOWN.name();
    }
}
