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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.master.repository.AuthorityRepository;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link OssAuthority} and the database authority catalog.
 *
 * @author Nikita Kirillov
 */
@DatabaseMatrixTest
@SuppressWarnings("removal")
class OssAuthorityTest {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Test
    void codenamesMatchDatabaseAuthorities() {
        // given.
        List<String> codenames = Arrays.stream(OssAuthority.values())
                .map(OssAuthority::getCodename)
                .toList();

        // when.
        List<DefaultAuthority> authorities = authorityRepository.findAll();

        // then.
        assertThat(authorities).extracting(DefaultAuthority::getName).containsExactlyInAnyOrderElementsOf(codenames);
    }
}
