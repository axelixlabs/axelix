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
package com.axelixlabs.axelix.master.service.state.users;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MySQLContainer;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.axelixlabs.axelix.master.service.state.DatabaseUserService;

/**
 * MySQL integration tests for {@link DatabaseUserService}.
 *
 * @see DatabaseUserService
 * @author Sergey Cherkasov
 */
class MySqlDatabaseUserServiceTest extends DatabaseUserServiceTest {

    @ServiceConnection
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("axelix")
            .withUsername("axelix")
            .withPassword("axelix");

    @BeforeAll
    static void beforeAll() {
        MYSQL.start();
    }

    @AfterAll
    static void afterAll() {
        MYSQL.stop();
    }

    // We cannot go got @ServiceConnection since this mechanism bypasses the spring.datasource.* settings,
    // and thus the wrong AutoConfiguration got fired
    @DynamicPropertySource
    static void register(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> MYSQL.getJdbcUrl());
        registry.add("spring.datasource.username", () -> MYSQL.getUsername());
        registry.add("spring.datasource.password", () -> MYSQL.getPassword());
    }
}
