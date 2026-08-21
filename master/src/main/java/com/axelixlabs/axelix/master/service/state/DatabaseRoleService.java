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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.repository.RoleRepository;
import com.axelixlabs.axelix.master.repository.RoleRepository.RoleWithAuthorityName;

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
        List<RoleWithAuthorityName> rows = roleRepository.findWithAuthoritiesByName(name);

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Set<Authority> authorities = rows.stream()
                .flatMap(row -> Optional.ofNullable(row.authorityName()).stream())
                .map(authorityName -> (Authority) () -> authorityName)
                .collect(Collectors.toSet());

        return Optional.of(new DefaultRole(name, authorities));
    }

    @Override
    public Set<Role> findRolesOfUser(String userId) {
        List<RoleWithAuthorityName> rows = roleRepository.findWithAuthoritiesByUserId(userId);
        Map<String, Set<Authority>> authoritiesByRole = new LinkedHashMap<>();

        for (RoleWithAuthorityName row : rows) {
            Set<Authority> authorities = authoritiesByRole.computeIfAbsent(row.roleName(), _ -> new HashSet<>());
            if (row.authorityName() != null) {
                String authorityName = row.authorityName();
                authorities.add(() -> authorityName);
            }
        }
        return authoritiesByRole.entrySet().stream()
                .map(entry -> (Role) new DefaultRole(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }
}
