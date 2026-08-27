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
package com.axelixlabs.axelix.common.auth.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;

/**
 * Default implementation of {@link AuthorityDecoder}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultAuthorityDecoder implements AuthorityDecoder {

    private final Map<String, Authority> effectiveAuthorities;

    public DefaultAuthorityDecoder(@Nullable AuthoritiesContributor authoritiesContributor) {
        Set<Authority> effectiveAuthorities = new HashSet<>(DefaultAuthority.asSet());

        if (authoritiesContributor != null) {

            Set<Authority> additionalAuthorities = authoritiesContributor.contribute();

            if (additionalAuthorities != null && !additionalAuthorities.isEmpty()) {
                effectiveAuthorities.addAll(additionalAuthorities);
            }
        }

        this.effectiveAuthorities =
                effectiveAuthorities.stream().collect(Collectors.toMap(Authority::getName, Function.identity()));
    }

    @Override
    @Nullable
    public Authority decode(String name) {
        return effectiveAuthorities.get(name);
    }

    /**
     * An SPI interface meant to be implemented by anybody who wants to contribute
     * authorities into the {@link DefaultAuthorityDecoder}.
     *
     * @author Mikhail Polivakha
     */
    public interface AuthoritiesContributor {

        /**
         * @return authorities to contribute
         */
        Set<Authority> contribute();
    }
}
