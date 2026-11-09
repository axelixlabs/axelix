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
package com.axelixlabs.axelix.master.service.auth;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.service.tech.TechUserJwtSerializer;
import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.domain.auth.DefaultRole;
import com.axelixlabs.axelix.master.domain.auth.OssAuthority;
import com.axelixlabs.axelix.master.domain.auth.PasswordlessUser;
import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.domain.auth.SecurityContextExecutor;
import com.axelixlabs.axelix.master.service.auth.jwt.UserJwtDecoderService;
import com.axelixlabs.axelix.master.service.auth.jwt.UserJwtEncoderService;
import com.axelixlabs.axelix.master.utils.SemanticVersion;

/**
 * Selects outbound authentication according to the target starter's protocol version.
 */
public class StarterAuthenticationTokenProvider {

    /**
     * Legacy representation of the master identity for initial discovery, before the starter version is known.
     * Older starters require the legacy claims; starters from 1.3 accept the master subject and ignore those
     * additional claims. The roles are empty because metadata discovery requires no authorities.
     *
     * @deprecated remove in 1.6, when all starters in the compatibility window support technical-user tokens
     */
    @Deprecated(forRemoval = true, since = "1.3")
    private static final PasswordlessUser DISCOVERY_USER =
            new PasswordlessUser(TechUser.MASTER.getId(), TechUser.MASTER.getId(), Set.of());

    private final SecurityContextExecutor securityContextExecutor;
    private final UserJwtEncoderService userJwtEncoderService;
    private final TechUserJwtSerializer techUserJwtSerializer;

    private final UserJwtDecoderService userJwtDecoderService;

    public StarterAuthenticationTokenProvider(
            SecurityContextExecutor securityContextExecutor,
            UserJwtEncoderService userJwtEncoderService,
            TechUserJwtSerializer techUserJwtSerializer,
            UserJwtDecoderService userJwtDecoderService) {
        this.securityContextExecutor = securityContextExecutor;
        this.userJwtEncoderService = userJwtEncoderService;
        this.techUserJwtSerializer = techUserJwtSerializer;
        this.userJwtDecoderService = userJwtDecoderService;
    }

    /**
     * Reissues the caller's token with legacy authority names for starters before 1.3, which enforce RBAC locally,
     * or a master technical-user token for starters from 1.3, including prereleases.
     *
     * @throws IllegalArgumentException if the starter version cannot be parsed
     */
    public String tokenFor(String starterVersion) {
        SemanticVersion version = SemanticVersion.parse(starterVersion);
        if (version.major() > 1 || (version.major() == 1 && version.minor() >= 3)) {
            return techUserJwtSerializer.serialize(TechUser.MASTER);
        }
        // Remove this legacy branch and the version check in 1.6, when starters before 1.3 leave the compatibility
        // window.
        PasswordlessUser caller = userJwtDecoderService.decodeTokenToUser(
                securityContextExecutor.requireSecurityContext().token());
        Set<Role> roles = caller.getRoles().stream().map(this::legacyRole).collect(Collectors.toSet());
        return userJwtEncoderService.generateToken(
                new PasswordlessUser(caller.getId(), caller.getUsername(), roles), Duration.ofMinutes(5));
    }

    private Role legacyRole(Role role) {
        Set<Authority> authorities = role.authorities().stream()
                .flatMap(authority -> Arrays.stream(OssAuthority.values())
                        .filter(legacy -> legacy.getCodename().equals(authority.getName())))
                .collect(Collectors.toSet());
        Set<Role> components = role.components().stream().map(this::legacyRole).collect(Collectors.toSet());
        return new DefaultRole(role.name(), authorities, components);
    }

    /**
     * Initial discovery has no starter version yet. A legacy master token supplies the uid and
     * roles required by older decoders, and the master subject accepted by the 1.3 decoder.
     *
     * @deprecated replace the legacy encoding with a master technical-user token in 1.6.
     *             Discovery authentication remains necessary; see {@link #DISCOVERY_USER}.
     */
    @Deprecated(forRemoval = true, since = "1.3")
    public String discoveryToken() {
        return userJwtEncoderService.generateToken(DISCOVERY_USER, Duration.ofMinutes(5));
    }
}
