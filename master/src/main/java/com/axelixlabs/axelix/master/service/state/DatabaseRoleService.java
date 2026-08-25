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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.service.AuthoritiesManager;
import com.axelixlabs.axelix.master.repository.RoleRepository;
import com.axelixlabs.axelix.master.repository.RoleRepository.RoleParentBond;
import com.axelixlabs.axelix.master.repository.RoleRepository.RoleWithAuthorityName;

/**
 * JDBC-based implementation of {@link RoleService} that reads roles from the {@code roles}, {@code roles_authorities},
 * {@code roles_parents} and {@code authorities} tables.
 *
 * @author Sergey Cherkasov
 */
@Service
@NullMarked
@Transactional
public class DatabaseRoleService implements RoleService {

    private final RoleRepository roleRepository;
    private final AuthoritiesManager authoritiesManager;

    public DatabaseRoleService(RoleRepository roleRepository, AuthoritiesManager authoritiesManager) {
        this.roleRepository = roleRepository;
        this.authoritiesManager = authoritiesManager;
    }

    @Override
    public Optional<Role> findByName(String name) {
        RoleGraph graph = readGraph();

        return findRoleId(graph, name).flatMap(roleId -> composeRole(graph, roleId));
    }

    @Override
    public Set<Role> findRolesOfUser(String userId) {
        RoleGraph graph = readGraph();

        return roleRepository.findRoleIdsOfUser(userId).stream()
                .map(roleId -> composeRole(graph, roleId))
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    private RoleGraph readGraph() {
        Map<String, String> names = new HashMap<>();
        Map<String, Set<Authority>> authorities = new HashMap<>();

        for (RoleWithAuthorityName row : roleRepository.findAllWithAuthorities()) {
            names.put(row.roleId(), row.roleName());
            Set<Authority> granted = authorities.computeIfAbsent(row.roleId(), _ -> new HashSet<>());

            if (row.authorityName() != null) {
                String authorityName = row.authorityName();
                granted.add(() -> authorityName);
            }
        }

        Map<String, List<String>> parents = new HashMap<>();

        for (RoleParentBond bond : roleRepository.findAllParentBonds()) {
            parents.computeIfAbsent(bond.roleId(), _ -> new ArrayList<>()).add(bond.parentRoleId());
        }

        return new RoleGraph(names, authorities, parents);
    }

    private Optional<String> findRoleId(RoleGraph graph, String roleName) {
        return graph.names().entrySet().stream()
                .filter(entry -> entry.getValue().equals(roleName))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private Optional<Role> composeRole(RoleGraph graph, String roleId) {
        return composeRole(graph, roleId, new HashSet<>());
    }

    private Optional<Role> composeRole(RoleGraph graph, String roleId, Set<String> derivationPath) {
        String roleName = graph.names().get(roleId);

        // The roles, the bonds and the roles of the user are three separate reads rather than one snapshot, so an id
        // can point at a role the roles query did not return. Such a role is left out - this runs on the login path,
        // and it is not worth an unresolvable id there
        if (roleName == null) {
            return Optional.empty();
        }

        derivationPath.add(roleId);
        Set<Role> components = new HashSet<>();

        for (String parentRoleId : graph.parents().getOrDefault(roleId, List.of())) {
            if (derivationPath.add(parentRoleId)) {
                composeRole(graph, parentRoleId, derivationPath).ifPresent(components::add);
                derivationPath.remove(parentRoleId);
            }
        }

        derivationPath.remove(roleId);

        return Optional.of(new DefaultRole(roleName, graph.authorities().getOrDefault(roleId, Set.of()), components));
    }

    /**
     * The roles and the bonds between them, as read from the database, ready to be resolved into composite roles.
     */
    private record RoleGraph(
            Map<String, String> names, Map<String, Set<Authority>> authorities, Map<String, List<String>> parents) {}
}
