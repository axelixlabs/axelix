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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;

// TODO: Add Azul GC
// TODO: Are we sure that the aliases provided here are even correct? Double check
/**
 * Gar
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public enum GarageCollector {

    /**
     * G1 GC
     */
    G1("g1"),

    /**
     * Shenandoah GC (Non-generational)
     */
    SHENADOAH("shenandoah"),

    /**
     * Generational shenandoah GC
     */
    GEN_SHENADOAH("generational shenandoah"),

    /**
     * Oracle ZGC GC
     */
    ZGC("zgc"),

    /**
     * Epsilon GC
     */
    EPSILONGC("epsilongc", "epsilon"),

    /**
     * Parallel collector
     */
    PARALLEL("parallel", "ps marksweep", "ps scavenge"),

    /**
     * Serial collector
     */
    SERIAL("marksweepcompact", "copy"),

    /**
     * Concurrent Mark and Sweep collector.
     */
    CMS("concurrent", "parnew"),

    /**
     * Unknown GC.
     */
    UNKNOWN();

    GarageCollector(String... aliases) {
        this.aliases = Arrays.stream(aliases).collect(Collectors.toSet());
    }

    private final Set<String> aliases;

    public static GarageCollector fromName(String name) {
        String lowerCase = name.toLowerCase();

        for (GarageCollector value : values()) {
            for (String alias : value.aliases) {
                if (lowerCase.contains(alias)) {
                    return value;
                }
            }
        }

        return GarageCollector.UNKNOWN;
    }
}
