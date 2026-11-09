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

import java.util.HashSet;
import java.util.Set;

import com.axelixlabs.axelix.common.utils.Lazy;
import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.repository.AuthorityRepository;

/**
 * Database-backed implementation of {@link AuthoritiesService}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultAuthoritiesService implements AuthoritiesService {

    private final Lazy<Set<Authority>> authorities;

    public DefaultAuthoritiesService(AuthorityRepository authorityRepository) {
        this.authorities = Lazy.of(() -> new HashSet<>(authorityRepository.findAll()));
    }

    @Override
    public Set<Authority> getAll() {
        return authorities.require();
    }
}
