/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.integrations.rdbms;

import java.util.Set;

import javax.sql.DataSource;

/**
 * Represents different types of the relational databases Axile can work with.
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
