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
package com.axelixlabs.axelix.master.app.persistence;

import java.util.Map;

import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.axelixlabs.axelix.master.autoconfiguration.database.LiquibasePropertiesBuilder;
import com.axelixlabs.axelix.master.domain.database.OssRdbms;

/**
 * Persistence-related configuration for the OSS distribution.
 *
 * @author Mikhail Polivakha
 */
@Configuration
public class OssPersistenceConfiguration {

    private static final Map<OssRdbms, String> CHANGE_LOGS = Map.of(
            OssRdbms.SQLITE, "db/changelog/sqlite/db.changelog.sqlite.xml",
            OssRdbms.POSTGRES, "db/changelog/postgres/db.changelog.postgresql.xml",
            OssRdbms.MYSQL, "db/changelog/mysql/db.changelog.mysql.xml");

    @Bean
    @Primary
    public LiquibaseProperties liquibaseProperties(LiquibasePropertiesBuilder liquibasePropertiesBuilder) {
        return liquibasePropertiesBuilder.forActiveRdbms(CHANGE_LOGS);
    }
}
