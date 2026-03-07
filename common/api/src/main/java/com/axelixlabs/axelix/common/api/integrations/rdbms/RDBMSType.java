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
package com.axelixlabs.axelix.common.api.integrations.rdbms;

import java.util.Set;

import javax.sql.DataSource;

/**
 * Represents different types of the relational databases Axelix can work with.
 *
 * @since 07.07.2025
 * @author Mikhail Polivakha
 */
public enum RDBMSType {
    POSTGRESQL(Set.of("Postgres", "PostgreSQL"), "PostgreSQL Database"),
    ORACLE(Set.of("Oracle", "Ora"), "Oracle Database"),
    MY_SQL(Set.of("MySQL", "MySql"), "MySQL Database"),
    SQL_SERVER(Set.of("Sql Server", "MSSQL", "MS SQL", "Yukon", "Katmai", "Denali"), "Sql Server Database"),
    ;

    /**
     * Aliases names for this database.
     */
    private final Set<String> aliases;

    /**
     * Checks whether the given {@link DataSource} provides connections for given database.
     */
    private final String displayName;

    RDBMSType(Set<String> aliases, String displayName) {
        this.aliases = aliases;
        this.displayName = displayName;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public String getDisplayName() {
        return displayName;
    }
}
