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

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.domain.auth.DefaultAuthority;
import com.axelixlabs.axelix.master.domain.auth.DefaultRole;
import com.axelixlabs.axelix.master.domain.auth.PasswordlessUser;
import com.axelixlabs.axelix.master.service.auth.jwt.UserJwtDecoderService;
import com.axelixlabs.axelix.master.service.auth.jwt.UserJwtEncoderService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultAuthoritiesService}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
@Transactional
class DefaultAuthoritiesServiceTest {
    @Autowired
    private AuthoritiesService authoritiesService;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private UserJwtEncoderService encoder;

    @Autowired
    private UserJwtDecoderService decoder;

    @Test
    void shouldDecodeJwtWithAuthorityDefinedOnlyInDatabase() {
        // given.
        DefaultAuthority authority = insertAuthority("custom:jwt");
        PasswordlessUser user =
                new PasswordlessUser("test-user", "test", Set.of(new DefaultRole("CUSTOM", Set.of(authority))));
        String token = encoder.generateToken(user);
        // when.
        PasswordlessUser decoded = decoder.decodeTokenToUser(token);
        // then.
        assertThat(decoded.getRoles())
                .singleElement()
                .satisfies(role -> assertThat(role.getAuthorities()).containsExactly(authority));
    }

    @Test
    void shouldDecodeAuthorityDefinedOnlyInDatabase() {
        // given.
        DefaultAuthority authority = insertAuthority("custom:read");
        // when.
        Authority decoded = authoritiesService.decode(authority.codename());
        // then.
        assertThat(decoded).isEqualTo(authority);
    }

    @Test
    void shouldReturnNullForUnknownCodename() {
        // when.
        Authority decoded = authoritiesService.decode("unknown:authority");
        // then.
        assertThat(decoded).isNull();
    }

    @Test
    void shouldNotResolveLegacyEnumName() {
        // when.
        Authority decoded = authoritiesService.decode("CACHES_CLEAR");
        // then.
        assertThat(decoded).isNull();
    }

    @Test
    void shouldReturnAllAuthoritiesFromDatabase() {
        // given.
        DefaultAuthority authority = insertAuthority("custom:list");
        // when.
        Set<Authority> authorities = authoritiesService.getAll();
        // then.
        assertThat(authorities).contains(authority);
        assertThat(authorities)
                .extracting(Authority::getName)
                .containsExactlyInAnyOrderElementsOf(jdbcClient
                        .sql("SELECT codename FROM authorities")
                        .query(String.class)
                        .list());
    }

    @Test
    void shouldReflectDatabaseUpdates() {
        // given.
        DefaultAuthority authority = insertAuthority("custom:before");
        authoritiesService.getAll();
        jdbcClient
                .sql("UPDATE authorities SET codename = ? WHERE id = ?")
                .params("custom:after", authority.id())
                .update();
        // when.
        Authority decoded = authoritiesService.decode("custom:after");
        // then.
        assertThat(decoded).isEqualTo(new DefaultAuthority(authority.id(), "custom:after"));
    }

    @Test
    void shouldReturnEmptyCatalogWhenTableIsEmpty() {
        // given.
        jdbcClient.sql("DELETE FROM roles_authorities").update();
        jdbcClient.sql("DELETE FROM authorities").update();
        // when.
        Set<Authority> authorities = authoritiesService.getAll();
        // then.
        assertThat(authorities).isEmpty();
    }

    private DefaultAuthority insertAuthority(String codename) {
        DefaultAuthority authority = new DefaultAuthority(UUID.randomUUID().toString(), codename);
        jdbcClient
                .sql("INSERT INTO authorities (id, codename, description) VALUES (?, ?, ?)")
                .params(authority.id(), codename, "Test authority")
                .update();
        return authority;
    }
}
