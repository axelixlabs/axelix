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
package com.axelixlabs.axelix.master.domain.dependencies;

/**
 * Where a team should look once a project is no longer {@link SupportStatus#ACTIVE}.
 * <p>
 * The {@link #value()} is prose rather than {@link ArtifactCoordinates} on purpose. A successor is not always another
 * Maven artifact - it can be the JDK itself ({@code java.time}), a whole version line under different coordinates
 * ({@code org.ehcache:ehcache 3.x}), or nothing concrete at all.
 *
 * @param kind  how the value should be read, and therefore how it is labelled in the UI
 * @param value the successor, or the reason there is not one
 *
 * @author Mikhail Polivakha
 */
public record Succession(Kind kind, String value) {

    public static Succession supersededBy(String value) {
        return new Succession(Kind.SUPERSEDED_BY, value);
    }

    public static Succession noDirectReplacement(String value) {
        return new Succession(Kind.NO_DIRECT_REPLACEMENT, value);
    }

    public enum Kind {

        /**
         * There is a named drop-in successor to migrate to.
         */
        SUPERSEDED_BY,

        /**
         * Nothing replaces the project one-to-one, so the value explains what replacing it actually involves.
         */
        NO_DIRECT_REPLACEMENT
    }
}
