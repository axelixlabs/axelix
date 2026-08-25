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
package com.axelixlabs.axelix.common.auth.core;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests of {@link Role#getEffectiveAuthorities()}, the single definition of what a role grants. Everyone who
 * decides what the holder of a role may do goes through it, so the union it returns is what access ends up being.
 *
 * @author Sergey Cherkasov
 */
class DefaultRoleTest {

    @Test
    void shouldReturnItsOwnAuthoritiesWhenItDerivesFromNobody() {
        // given.
        Role role = new DefaultRole("VIEWER", Set.of(DefaultAuthority.CACHES_TOGGLE));

        // when & then.
        assertThat(role.getEffectiveAuthorities())
                .extracting(Authority::getName)
                .containsExactly(DefaultAuthority.CACHES_TOGGLE.name());
    }

    @Test
    void shouldUniteTheAuthoritiesOfTheWholeChainItDerivesFrom() {
        // given. C derives from B, B derives from A
        Role a = new DefaultRole("A", Set.of(DefaultAuthority.ENV_VALUES_READ));
        Role b = new DefaultRole("B", Set.of(DefaultAuthority.CACHES_CLEAR), Set.of(a));
        Role c = new DefaultRole("C", Set.of(DefaultAuthority.GARBAGE_COLLECTOR), Set.of(b));

        // when & then. The walk goes all the way down rather than one level
        assertThat(c.getEffectiveAuthorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrder(
                        DefaultAuthority.GARBAGE_COLLECTOR.name(),
                        DefaultAuthority.CACHES_CLEAR.name(),
                        DefaultAuthority.ENV_VALUES_READ.name());
    }

    @Test
    void shouldCountTheRoleReachedThroughSeveralBranchesOnce() {
        // given. A diamond: the top role is reached through both of them
        Role top = new DefaultRole("TOP", Set.of(DefaultAuthority.ENV_VALUES_READ));
        Role left = new DefaultRole("LEFT", Set.of(DefaultAuthority.CACHES_CLEAR), Set.of(top));
        Role right = new DefaultRole("RIGHT", Set.of(DefaultAuthority.CACHES_TOGGLE), Set.of(top));
        Role bottom = new DefaultRole("BOTTOM", Set.of(DefaultAuthority.GARBAGE_COLLECTOR), Set.of(left, right));

        // when & then. Reaching the same authority twice neither duplicates it nor loses the rest
        assertThat(bottom.getEffectiveAuthorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrder(
                        DefaultAuthority.GARBAGE_COLLECTOR.name(),
                        DefaultAuthority.CACHES_CLEAR.name(),
                        DefaultAuthority.CACHES_TOGGLE.name(),
                        DefaultAuthority.ENV_VALUES_READ.name());
    }
}
