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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.exception.auth.UserNotFoundException;
import com.axelixlabs.axelix.master.repository.AuthorityRepository.AuthorityProfile;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.state.auth.RoleService;

/**
 * Resolves a managed user's effective authorities against the full authority catalog.
 */
@Service
@Transactional(readOnly = true)
public class UserAuthoritiesService {

    private final AuthoritiesService authoritiesService;
    private final RoleService roleService;
    private final UserRepository userRepository;

    public UserAuthoritiesService(
            AuthoritiesService authoritiesService, RoleService roleService, UserRepository userRepository) {
        this.authoritiesService = authoritiesService;
        this.roleService = roleService;
        this.userRepository = userRepository;
    }

    /**
     * Partitions the catalog into effective and ungranted authorities, each ordered by codename.
     * Effective authorities include grants inherited through component roles.
     *
     * @throws UserNotFoundException if the user does not exist
     */
    public UserAuthorities getAuthorities(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        Set<String> granted = roleService.findRolesOfUser(userId).stream()
                .flatMap(role -> role.getEffectiveAuthorities().stream())
                .map(Authority::getName)
                .collect(Collectors.toSet());
        List<AuthorityProfile> catalog = authoritiesService.getAuthoritiesFeed();

        return new UserAuthorities(
                catalog.stream()
                        .filter(authority -> granted.contains(authority.codename()))
                        .toList(),
                catalog.stream()
                        .filter(authority -> !granted.contains(authority.codename()))
                        .toList());
    }

    public record UserAuthorities(
            List<AuthorityProfile> effectiveAuthorities, List<AuthorityProfile> ungrantedAuthorities) {}
}
