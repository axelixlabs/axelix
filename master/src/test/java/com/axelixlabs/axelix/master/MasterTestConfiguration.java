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
package com.axelixlabs.axelix.master;

import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.axelixlabs.axelix.master.api.external.response.LicensingInfoResponse;
import com.axelixlabs.axelix.master.autoconfiguration.database.ConditionalOnCommunityRdbms;
import com.axelixlabs.axelix.master.domain.database.OssRdbms;
import com.axelixlabs.axelix.master.service.LicensingInfoResolver;

/**
 * Test Configuration.
 *
 * @author Nikita Kirillov
 */
@Configuration
public class MasterTestConfiguration {

    @Bean
    public LicensingInfoResolver licensingInfoResolver() {
        return LicensingInfoResponse::oss;
    }

    @Configuration
    @ConditionalOnCommunityRdbms(OssRdbms.SQLITE)
    public static class SQLiteTestLiquibaseAutoConfiguration {

        @Bean
        @Primary
        public LiquibaseProperties sqliteLiquibaseProperties() {
            LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
            liquibaseProperties.setChangeLog("db/changelog/sqlite/db.changelog.master.sqlite.xml");
            return liquibaseProperties;
        }
    }

    @Configuration
    @ConditionalOnCommunityRdbms(OssRdbms.POSTGRES)
    public static class PostgresTestLiquibaseAutoConfiguration {

        @Bean
        @Primary
        public LiquibaseProperties postgresLiquibaseProperties() {
            LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
            liquibaseProperties.setChangeLog("db/changelog/postgres/db.changelog.master.postgresql.xml");
            return liquibaseProperties;
        }
    }

    @Configuration
    @ConditionalOnCommunityRdbms(OssRdbms.MYSQL)
    public static class MySqlTestLiquibaseAutoConfiguration {

        @Bean
        @Primary
        public LiquibaseProperties mysqlLiquibaseProperties() {
            LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
            liquibaseProperties.setChangeLog("db/changelog/mysql/db.changelog.master.mysql.xml");
            return liquibaseProperties;
        }
    }
}
