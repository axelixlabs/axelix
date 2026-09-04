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
package com.axelixlabs.axelix.master.autoconfiguration.database;

import java.util.Map;

import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;

import com.axelixlabs.axelix.master.domain.database.OssRdbms;

/**
 * An abstraction that is capable to build the correct {@link LiquibaseProperties} depending on the database in use.
 *
 * @author Mikhail Polivakha
 */
public final class MasterLiquibasePropertiesBuilder {

    private MasterLiquibasePropertiesBuilder() {}

    /**
     * @param jdbcUrl the resolved JDBC url.
     * @param changeLogByRdbms root changelog location per supported {@link OssRdbms}
     * @param credentials credentials Liquibase authenticates with (applied only for networked databases)
     */
    public static LiquibaseProperties forActiveRdbms(
            String jdbcUrl, Map<OssRdbms, String> changeLogByRdbms, AxelixMigrationProperties credentials) {
        OssRdbms rdbms = OssRdbms.fromJdbcUrl(jdbcUrl);

        String changeLog = changeLogByRdbms.get(rdbms);
        if (changeLog == null) {
            throw new IllegalArgumentException(
                    "No root changelog configured for the active RDBMS '%s'".formatted(rdbms));
        }

        LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
        liquibaseProperties.setChangeLog(changeLog);

        // SQLite is an embedded, file-based database and authenticates through the datasource connection only.
        if (rdbms != OssRdbms.SQLITE) {
            liquibaseProperties.setUser(credentials.username());
            liquibaseProperties.setPassword(credentials.password());
        }

        return liquibaseProperties;
    }
}
