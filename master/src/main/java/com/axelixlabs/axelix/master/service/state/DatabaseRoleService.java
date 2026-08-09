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
package com.axelixlabs.axelix.master.service.state;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.domain.RoleEntity;
import com.axelixlabs.axelix.master.repository.RoleRepository;

/**
 * JDBC-based implementation of {@link RoleService} that reads roles from the {@code roles}, {@code roles_authorities}
 * and {@code authorities} tables.
 *
 * @author Sergey Cherkasov
 */
@Service
@NullMarked
@Transactional
public class DatabaseRoleService implements RoleService {

    private final RoleRepository roleRepository;

    public DatabaseRoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name).map(this::toRole);
    }

    private Role toRole(RoleEntity roleEntity) {
        return new DefaultRole(roleEntity.name(), resolveAuthorities(roleEntity.name()));
    }

    private Set<Authority> resolveAuthorities(String roleName) {
        return roleRepository.findAuthorityNamesByRoleName(roleName).stream()
                .map(this::safeAuthorityFromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Nullable
    private Authority safeAuthorityFromString(String name) {
        try {
            return DefaultAuthority.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
