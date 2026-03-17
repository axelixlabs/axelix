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
package com.axelixlabs.axelix.master.autoconfiguration;

import org.jspecify.annotations.NonNull;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.axelixlabs.axelix.master.repository.dialect.SQLiteDialect;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Autoconfiguration for {@link InstanceRegistry} persistence layer.
 *
 * @since 12.03.2026
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class PersistenceAutoConfiguration {

    /**
     * Autoconfiguration for SQLite-based {@link InstanceRegistry}.
     */
    @AutoConfiguration
    @ConditionalOnClass(name = "org.sqlite.JDBC")
    public static class SQLiteAutoConfiguration extends AbstractJdbcConfiguration {

        @Bean
        @Override
        public @NonNull Dialect jdbcDialect(@NonNull NamedParameterJdbcOperations operations) {
            return new SQLiteDialect();
        }
    }
}
