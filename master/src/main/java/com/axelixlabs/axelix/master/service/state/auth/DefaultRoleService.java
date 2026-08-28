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
package com.axelixlabs.axelix.master.service.state.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import com.axelixlabs.axelix.common.auth.service.AuthoritiesManager;
import com.axelixlabs.axelix.master.repository.RoleRepository;
import com.axelixlabs.axelix.master.repository.RoleRepository.RoleParentBond;
import com.axelixlabs.axelix.master.repository.RoleRepository.RoleWithAuthorityName;

/**
 * JDBC-based implementation of {@link RoleService} that reads roles from the {@code roles}, {@code roles_authorities},
 * {@code roles_parents} and {@code directAuthorities} tables.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@Service
@NullMarked
@Transactional
public class DefaultRoleService implements RoleService {

    private final RoleRepository roleRepository;
    private final AuthoritiesManager authoritiesManager;

    public DefaultRoleService(RoleRepository roleRepository, AuthoritiesManager authoritiesManager) {
        this.roleRepository = roleRepository;
        this.authoritiesManager = authoritiesManager;
    }

    @Override
    public Optional<Role> findByName(String name) throws IllegalStateException {
        return getGraph().composeRoleFromName(name);
    }

    @Override
    public Set<Role> findRolesOfUser(String userId) throws IllegalStateException {
        RoleGraph graph = getGraph();

        return roleRepository.findRoleIdsOfUser(userId).stream()
                .map(graph::composeRole)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    private RoleGraph getGraph() {
        Map<String, String> names = new HashMap<>();
        Map<String, Set<Authority>> authorities = new HashMap<>();

        for (RoleWithAuthorityName row : roleRepository.findAllWithAuthorities()) {
            names.put(row.roleId(), row.roleName());
            Set<Authority> granted = authorities.computeIfAbsent(row.roleId(), _ -> new HashSet<>());

            if (row.authorityName() != null) {
                Optional
                    .ofNullable(authoritiesManager.decode(row.authorityName()))
                    .ifPresent(granted::add);
            }
        }

        Map<String, List<String>> parents = new HashMap<>();

        for (RoleParentBond bond : roleRepository.findAllParentBonds()) {
            parents.computeIfAbsent(bond.roleId(), _ -> new ArrayList<>()).add(bond.parentRoleId());
        }

        return new RoleGraph(names, authorities, parents);
    }

    /**
     * The roles and the bonds between them, as read from the database, ready to be resolved into composite roles.
     */
    private record RoleGraph(
        Map<String, String> roleIdToName,
        Map<String, Set<Authority>> directAuthorities,
        Map<String, List<String>> directParents) {

        private Optional<String> findRoleId(String roleName) {
            return roleIdToName.entrySet().stream()
                .filter(entry -> entry.getValue().equals(roleName))
                .map(Map.Entry::getKey)
                .findFirst();
        }

        private Optional<Role> composeRoleFromName(String roleName) {
            return findRoleId(roleName).flatMap(roleId -> composeRole(roleId, new HashSet<>()));
        }

        private Optional<Role> composeRole(String roleId) {
            return composeRole(roleId, new HashSet<>());
        }

        private Optional<Role> composeRole(String roleId, Set<String> visited) {
            String roleName = roleIdToName.get(roleId);

            if (roleName == null) {
                return Optional.empty();
            }

            visited.add(roleId);
            Set<Role> components = new HashSet<>();

            for (String parentRoleId : directParents.getOrDefault(roleId, List.of())) {
                if (visited.add(parentRoleId)) {
                    composeRole(parentRoleId, visited).ifPresent(components::add);
                } else {
                    // TODO:
                    //  ideally, we would want to create a descriptive message here explaining what
                    //  roles create a cycle. Like message with "A --> B --> C --> A" or something.
                    throw new IllegalStateException(
                        "Unable to resolve the state of the Role '%s': ".formatted(roleName) +
                        "it's ancestry create a cycle");
                }
            }

            return Optional.of(
                new DefaultRole(roleName, directAuthorities.getOrDefault(roleId, Set.of()), components)
            );
        }
    }
}
