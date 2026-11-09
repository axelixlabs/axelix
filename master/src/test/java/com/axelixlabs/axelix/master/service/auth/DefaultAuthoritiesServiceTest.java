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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.domain.auth.DefaultAuthority;
import com.axelixlabs.axelix.master.repository.AuthorityRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultAuthoritiesService}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
@Transactional
class DefaultAuthoritiesServiceTest {

    private AuthoritiesService authoritiesService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    @AfterEach
    void setUp() {
        jdbcClient.sql("DELETE FROM roles_authorities").update();
        jdbcClient.sql("DELETE FROM authorities").update();
        authoritiesService = new DefaultAuthoritiesService(authorityRepository);
    }

    @Test
    void shouldReturnAllAuthoritiesFromDatabase() {
        // given.
        DefaultAuthority authority = insertAuthority("custom:list");

        // when.
        Set<Authority> authorities = authoritiesService.getAll();

        // then.
        assertThat(authorities).containsOnly(authority);
    }

    @Test
    void shouldReturnEmptyCatalogWhenTableIsEmpty() {
        // when.
        Set<Authority> authorities = authoritiesService.getAll();

        // then.
        assertThat(authorities).isEmpty();
    }

    @Test
    void shouldCacheAuthorities() {
        // given.
        DefaultAuthority authority = insertAuthority("custom:list");
        authoritiesService.getAll();
        insertAuthority("custom:write");

        // when.
        Set<Authority> authorities = authoritiesService.getAll();

        // then.
        assertThat(authorities).containsOnly(authority);
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
