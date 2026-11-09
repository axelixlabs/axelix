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
package com.axelixlabs.axelix.master.domain.auth;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests of {@link Role#getEffectiveAuthorities()}, the single definition of what a role grants. Everyone who
 * decides what the holder of a role may do goes through it, so the union it returns is what access ends up being.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
class DefaultRoleTest {
    private Authority envValuesRead;
    private Authority cachesClear;
    private Authority cachesToggle;
    private Authority garbageCollector;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void insertAuthorities() {
        envValuesRead = insertAuthority("test-ENV_VALUES_READ", "test:environment.values:read");
        cachesClear = insertAuthority("test-CACHES_CLEAR", "test:caches:clear");
        cachesToggle = insertAuthority("test-CACHES_TOGGLE", "test:caches:toggle");
        garbageCollector = insertAuthority("test-GARBAGE_COLLECTOR", "test:gc:manage");
    }

    @AfterEach
    void deleteAuthorities() {
        jdbcClient
                .sql(
                        "DELETE FROM authorities WHERE id IN ('test-ENV_VALUES_READ', 'test-CACHES_CLEAR', 'test-CACHES_TOGGLE', 'test-GARBAGE_COLLECTOR')")
                .update();
    }

    @Test
    void shouldReturnItsOwnAuthoritiesWhenItDerivesFromNobody() {
        // given.
        Role role = new DefaultRole("VIEWER", Set.of(cachesToggle));

        // when & then.
        assertThat(role.getEffectiveAuthorities())
                .extracting(Authority::getName)
                .containsExactly("test:caches:toggle");
    }

    @Test
    void shouldUniteTheAuthoritiesOfTheWholeChainItDerivesFrom() {
        // given. C derives from B, B derives from A
        Role grandParent = new DefaultRole("A", Set.of(envValuesRead));
        Role parent = new DefaultRole("B", Set.of(cachesClear), Set.of(grandParent));
        Role child = new DefaultRole("C", Set.of(garbageCollector), Set.of(parent));

        // when & then. The walk goes all the way down rather than one level
        assertThat(child.getEffectiveAuthorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrder("test:gc:manage", "test:caches:clear", "test:environment.values:read");
    }

    @Test
    void shouldCountTheRoleReachedThroughSeveralBranchesOnce() {
        // given. A diamond: the top role is reached through both of them
        Role top = new DefaultRole("TOP", Set.of(envValuesRead));
        Role left = new DefaultRole("LEFT", Set.of(cachesClear), Set.of(top));
        Role right = new DefaultRole("RIGHT", Set.of(cachesToggle), Set.of(top));
        Role bottom = new DefaultRole("BOTTOM", Set.of(garbageCollector), Set.of(left, right));

        // when & then. Reaching the same authority twice neither duplicates it nor loses the rest
        assertThat(bottom.getEffectiveAuthorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrder(
                        "test:gc:manage", "test:caches:clear", "test:caches:toggle", "test:environment.values:read");
    }

    private Authority insertAuthority(String id, String codename) {
        jdbcClient
                .sql("INSERT INTO authorities (id, codename, description) VALUES (?, ?, ?)")
                .params(id, codename, "Test authority")
                .update();
        return jdbcClient
                .sql("SELECT id, codename FROM authorities WHERE id = ?")
                .param(id)
                .query((rs, n) -> new DefaultAuthority(rs.getString("id"), rs.getString("codename")))
                .single();
    }
}
