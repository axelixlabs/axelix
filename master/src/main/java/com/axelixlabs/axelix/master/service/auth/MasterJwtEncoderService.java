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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.JwtTokenGenerationException;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.master.contract.jwt.JwtRole;
import com.axelixlabs.axelix.master.contract.jwt.MasterJwtToken;

/**
 * Issues the master-produced JWT, {@link MasterJwtToken}.
 *
 * <p>The signed claims are taken from that contract model. Starters parse the same payload,
 * whose role values are the shared {@link JwtRole}.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public final class MasterJwtEncoderService implements JwtEncoderService {

    private final DefaultJwtEncoderService signer;

    private final Duration lifespan;

    public MasterJwtEncoderService(JwtAlgorithm algorithm, String signingKey, Duration lifespan) {
        this.signer = new DefaultJwtEncoderService(algorithm, signingKey, lifespan);
        this.lifespan = lifespan;
    }

    @Override
    public String generateToken(User user) throws JwtTokenGenerationException {
        return generateToken(user, lifespan);
    }

    @Override
    public String generateToken(User user, Duration lifespan) throws JwtTokenGenerationException {
        if (user == null) {
            throw new JwtTokenGenerationException("User cannot be null");
        }

        MasterJwtToken token = new MasterJwtToken()
                .sub(user.getUsername())
                .uid(user.getId())
                .roles(toRoles(user.getRoles()));

        return signer.sign(token.getSub(), token.getUid(), token.getRoles(), lifespan);
    }

    private List<JwtRole> toRoles(Set<Role> roles) {
        return roles.stream().map(this::toRole).collect(Collectors.toList());
    }

    private JwtRole toRole(Role role) {
        List<String> authorities =
                role.getAuthorities().stream().map(Authority::getName).collect(Collectors.toList());

        List<JwtRole> components = role.getComponents().stream().map(this::toRole).collect(Collectors.toList());

        return new JwtRole().name(role.getName()).authorities(authorities).components(components);
    }
}
