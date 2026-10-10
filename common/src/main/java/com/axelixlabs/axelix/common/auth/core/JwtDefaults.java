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
package com.axelixlabs.axelix.common.auth.core;

import java.time.Duration;

/**
 * Default JWT settings for Master and the starters, so that Axelix works out of the box
 * without configuring a signing key. The default key is publicly known and must be overridden in production.
 *
 * @author Sergey Cherkasov
 */
public final class JwtDefaults {

    private static final String VERY_IMPORTANT = "VERY IMPORTANT: ";

    public static final JwtAlgorithm DEFAULT_ALGORITHM = JwtAlgorithm.HMAC512;

    public static final String DEFAULT_SIGNING_KEY =
            "axelix-default-jwt-signing-key-publicly-known-change-me-before-production";

    public static final Duration MASTER_DEFAULT_LIFESPAN = Duration.ofHours(12);

    public static final String MASTER_DEFAULT_SIGNING_KEY_IN_USE_WARNING_MESSAGE = VERY_IMPORTANT
            + "Axelix Master is using the default JWT signing key, which is publicly known. "
            + "Set axelix.master.auth.jwt.signing-key before using Axelix in production";

    public static final String STARTER_DEFAULT_SIGNING_KEY_IN_USE_WARNING_MESSAGE = VERY_IMPORTANT
            + "This service is using the default JWT signing key, which is publicly known, "
            + "so anyone can forge a valid token. Set axelix.sbs.auth.jwt.signing-key "
            + "(the same key as in Axelix Master) before using Axelix in production";

    private JwtDefaults() {}

    public static boolean isDefaultSigningKey(String signingKey) {
        return DEFAULT_SIGNING_KEY.equals(signingKey);
    }
}
