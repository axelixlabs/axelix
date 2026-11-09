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
package com.axelixlabs.axelix.master.repository;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.axelixlabs.axelix.master.domain.auth.DefaultAuthority;

/**
 * Read access to the authorities catalog.
 *
 * @author Mikhail Polivakha
 */
@Repository
public class AuthorityRepository {
    private final JdbcClient jdbcClient;

    public AuthorityRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public record AuthorityProfile(String id, String codename, String displayName, String description) {}

    public List<AuthorityProfile> findAllProfiles() {
        return jdbcClient
                .sql("SELECT id, codename, display_name, description FROM authorities ORDER BY codename")
                .query((rs, rowNum) -> new AuthorityProfile(
                        rs.getString("id"),
                        rs.getString("codename"),
                        rs.getString("display_name"),
                        rs.getString("description")))
                .list();
    }

    public List<DefaultAuthority> findAll() {
        return jdbcClient
                .sql("SELECT id, codename FROM authorities")
                .query((rs, rowNum) -> new DefaultAuthority(rs.getString("id"), rs.getString("codename")))
                .list();
    }
}
