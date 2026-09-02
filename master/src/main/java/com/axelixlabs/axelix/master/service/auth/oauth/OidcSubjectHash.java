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
package com.axelixlabs.axelix.master.service.auth.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.jspecify.annotations.NullMarked;

/**
 * Computes the stable OIDC identity of a user as {@code SHA-256(iss + '\n' + sub)}.
 * <p>
 * The pair {@code (issuer, subject)} is the only identifier an OIDC provider guarantees to be stable and
 * non-reassignable per the OpenID Connect Core specification, whereas {@code preferred_username}/{@code name}
 * are mutable. Hashing the pair yields a compact, opaque key we can deduplicate users on independently of any
 * username change.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public final class OidcSubjectHash {

    private OidcSubjectHash() {}

    /**
     * @param issuer the {@code iss} claim (the OIDC provider identifier).
     * @param subject the {@code sub} claim (the provider-local, stable user identifier).
     * @return the hex-encoded SHA-256 of the issuer and subject.
     */
    public static String of(String issuer, String subject) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((issuer + '\n' + subject).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated to be available on every JVM, so this can never happen in practice.
            throw new IllegalStateException("SHA-256 message digest is not available", e);
        }
    }
}
