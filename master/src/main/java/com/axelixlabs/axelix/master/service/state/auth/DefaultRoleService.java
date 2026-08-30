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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
import com.axelixlabs.axelix.master.domain.RoleFeed;
import com.axelixlabs.axelix.master.repository.RoleRepository;
import com.axelixlabs.axelix.master.repository.RoleRepository.RoleMembers;
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

    @Override
    public List<RoleFeed> getRolesFeed() {
        Map<String, Integer> membersByRoleId = roleRepository.findAllMembersCounts().stream()
                .collect(Collectors.toMap(RoleMembers::roleId, RoleMembers::members));

        return roleRepository.findAll().stream()
                .map(role -> new RoleFeed(
                        role.id(), role.name(), membersByRoleId.getOrDefault(role.id(), 0), role.description()))
                .toList();
    }

    private RoleGraph getGraph() {
        Map<String, String> names = new HashMap<>();
        Map<String, Set<Authority>> authorities = new HashMap<>();

        for (RoleWithAuthorityName row : roleRepository.findAllWithAuthorities()) {
            names.put(row.roleId(), row.roleName());
            Set<Authority> granted = authorities.computeIfAbsent(row.roleId(), _ -> new HashSet<>());

            if (row.authorityName() != null) {
                Optional.ofNullable(authoritiesManager.decode(row.authorityName()))
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
            return findRoleId(roleName).flatMap(this::composeRole);
        }

        private Optional<Role> composeRole(String roleId) {
            return composeRole(roleId, new HashMap<>(), new ArrayDeque<>());
        }

        /**
         * Resolves the role identified by {@code roleId} into a composite {@link Role}, walking its ancestry
         * depth-first while guarding against cycles - the roles are meant to form a DAG. The algorithm below
         * is just a simple DFS with coloring (where we store already fully traversed vertexes, colloquially called
         * "Black", and those where we have not yet finished the traversal, colloquially called "Grey")
         * <p>
         * The two structures track the two states a DFS needs. A role sitting in {@code resolved} is done: it, along
         * with its whole ancestry, has been composed, so reaching it again is legitimate (a diamond simply unions the
         * authorities) and the memoised role is returned as is. A role sitting in {@code stack} is on the current DFS
         * traversal stack, so an edge back into it is a back-edge and therefore proves a cycle, on which we bail out with an
         * {@link IllegalStateException}. A role in neither is untouched and gets visited.
         *
         * @param resolved the roles already composed, memoised so a shared ancestor is built only once
         * @param stack     the ids of the roles currently on the DFS stack, in order, used both to spot a back-edge
         *                 and to describe the cycle it closes
         */
        private Optional<Role> composeRole(String roleId, Map<String, Role> resolved, Deque<String> stack) {
            String roleName = roleIdToName.get(roleId);

            if (roleName == null) {
                return Optional.empty();
            }

            Role alreadyResolved = resolved.get(roleId);

            if (alreadyResolved != null) {
                return Optional.of(alreadyResolved);
            }

            stack.addLast(roleId);
            Set<Role> components = new HashSet<>();

            for (String parentRoleId : directParents.getOrDefault(roleId, List.of())) {
                if (stack.contains(parentRoleId)) {
                    throw cycleDetected(stack, parentRoleId);
                }
                composeRole(parentRoleId, resolved, stack).ifPresent(components::add);
            }

            Role role = new DefaultRole(roleName, directAuthorities.getOrDefault(roleId, Set.of()), components);

            stack.removeLast();
            resolved.put(roleId, role);

            return Optional.of(role);
        }

        private IllegalStateException cycleDetected(Deque<String> path, String duplicatedRoleId) {
            List<String> ids = new ArrayList<>(path);
            String cycle = ids.subList(ids.indexOf(duplicatedRoleId), ids.size()).stream()
                    .map(id -> Objects.requireNonNull(roleIdToName.get(id)))
                    .collect(Collectors.joining(" --> "));

            return new IllegalStateException("Unable to resolve the state of the roles: their ancestry forms a cycle: "
                    + cycle + " --> " + roleIdToName.get(duplicatedRoleId));
        }
    }
}
