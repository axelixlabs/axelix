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
package com.axelixlabs.axelix.master.service.auth.encoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import static com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration.SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX;

/**
 * Password encoder wrap for {@link com.axelixlabs.axelix.master.service.auth.provider.SuperAdminUserAuthenticator}.
 *
 * @author Ilya Naumov
 */
@NullMarked
public class SuperAdminPasswordEncoder {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminPasswordEncoder.class);

    private final Set<String> supportedEncoderIds;
    private final DelegatingPasswordEncoder passwordEncoder;

    public SuperAdminPasswordEncoder(BCryptPasswordEncoder bcryptPasswordEncoder) {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        encoders.put("bcrypt", bcryptPasswordEncoder);
        this.passwordEncoder = new DelegatingPasswordEncoder("bcrypt", encoders);
        this.passwordEncoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());
        this.supportedEncoderIds = Set.copyOf(encoders.keySet());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void validatePasswordFormat(String password) {
        String encoderId = extractId(password);
        if (encoderId == null || Objects.equals(encoderId, "noop")) {
            log.warn(
                    "The {}.credentials.password is not hashed. Consider storing the password using a supported DelegatingPasswordEncoder format (e.g. {bcrypt}).",
                    SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX);
        } else {
            Assert.isTrue(
                    supportedEncoderIds.contains(encoderId),
                    "The " + SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX
                            + ".credentials.password uses the encoder id " + encoderId
                            + " which is unsupported.");
        }
    }

    public String extractEncodedPassword(String prefixEncodedPassword) {
        if (!prefixEncodedPassword.startsWith("{")) {
            return prefixEncodedPassword;
        }

        int end = prefixEncodedPassword.indexOf('}');
        if (end < 0) {
            return prefixEncodedPassword;
        }

        return prefixEncodedPassword.substring(end + 1);
    }

    private @Nullable String extractId(String prefixEncodedPassword) {
        int start = prefixEncodedPassword.indexOf("{");
        if (start != 0) {
            return null;
        }

        int end = prefixEncodedPassword.indexOf("}", start);
        if (end < 0) {
            return null;
        }

        return prefixEncodedPassword.substring(start + 1, end);
    }
}
