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
package com.axelixlabs.axelix.sbs.spring.core.cache;

import java.time.Instant;

/**
 * An object that holds information about the particular cache lookup.
 *
 * @author Mikhail Polivakha
 */
public class CacheLookup {

    private final Outcome outcome;
    private final Instant timestamp;

    public CacheLookup(Outcome outcome, Instant timestamp) {
        this.outcome = outcome;
        this.timestamp = timestamp;
    }

    public static CacheLookup miss() {
        return new CacheLookup(Outcome.MISS, Instant.now());
    }

    public static CacheLookup hit() {
        return new CacheLookup(Outcome.HIT, Instant.now());
    }

    public enum Outcome {
        HIT,
        MISS
    }

    public Outcome outcome() {
        return outcome;
    }

    public Instant timestamp() {
        return timestamp;
    }
}
