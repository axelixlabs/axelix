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

import java.sql.SQLException;

import javax.sql.DataSource;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.axelixlabs.axelix.master.repository.dialect.SQLiteDialect;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link PersistenceAutoConfiguration}.
 *
 * @author Mikhail Polivakha
 */
class PersistenceAutoConfigurationTest {

    private static final String SQLITE_CHANGELOG = "db/changelog/sqlite/db.changelog.master.sqlite.xml";
    private static final String POSTGRES_CHANGELOG = "db/changelog/postgres/db.changelog.master.postgresql.xml";
    private static final String MYSQL_CHANGELOG = "db/changelog/mysql/db.changelog.master.mysql.xml";

    private static ApplicationContextRunner baselineContextRunner() {
        return new ApplicationContextRunner(PersistenceAutoConfigurationTest::isolatedContext)
                .withUserConfiguration(TestPersistenceDependenciesConfig.class)
                .withConfiguration(AutoConfigurations.of(
                        PersistenceAutoConfiguration.class,
                        PersistenceAutoConfiguration.SQLiteAutoConfiguration.class,
                        PersistenceAutoConfiguration.PostgreSqlAutoConfiguration.class,
                        PersistenceAutoConfiguration.MySqlAutoConfiguration.class));
    }

    @Test
    void shouldCreateSqliteSpecificBeansWhenSqliteJdbcUrlIsConfigured() {
        // given.
        ApplicationContextRunner contextRunner = baselineContextRunner()
                .withPropertyValues("spring.datasource.url=jdbc:sqlite:file:memdb?mode=memory&cache=shared");

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasSingleBean(PersistenceAutoConfiguration.SQLiteAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(PersistenceAutoConfiguration.PostgreSqlAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(PersistenceAutoConfiguration.MySqlAutoConfiguration.class);

            assertThat(context).hasSingleBean(LiquibaseProperties.class);
            assertThat(context.getBean(LiquibaseProperties.class).getChangeLog())
                    .isEqualTo(SQLITE_CHANGELOG);

            assertThat(context).hasSingleBean(JdbcDialect.class);
            assertThat(context.getBean(JdbcDialect.class)).isInstanceOf(SQLiteDialect.class);
        });
    }

    @Test
    void shouldCreatePostgresSpecificBeansWhenPostgresJdbcUrlIsConfigured() {
        // given.
        ApplicationContextRunner contextRunner = baselineContextRunner()
                .withPropertyValues("spring.datasource.url=jdbc:postgresql://localhost:5432/axelix");

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasSingleBean(PersistenceAutoConfiguration.PostgreSqlAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(PersistenceAutoConfiguration.SQLiteAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(PersistenceAutoConfiguration.MySqlAutoConfiguration.class);

            assertThat(context).hasSingleBean(LiquibaseProperties.class);
            assertThat(context.getBean(LiquibaseProperties.class).getChangeLog())
                    .isEqualTo(POSTGRES_CHANGELOG);

            assertThat(context).hasSingleBean(JdbcDialect.class);
            assertThat(context.getBean(JdbcDialect.class)).isInstanceOf(JdbcPostgresDialect.class);
        });
    }

    @TestConfiguration
    static class TestPersistenceDependenciesConfig {

        @Bean
        public JsonMapper jsonMapper() {
            return new JsonMapper();
        }

        @Bean
        public NamedParameterJdbcOperations namedParameterJdbcOperations() throws SQLException {
            DataSource dataSource = Mockito.mock(DataSource.class);
            return new NamedParameterJdbcTemplate(dataSource);
        }
    }

    private static @NonNull AnnotationConfigApplicationContext isolatedContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        StandardEnvironment cleanEnv = new StandardEnvironment();
        cleanEnv.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        cleanEnv.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        context.setEnvironment(cleanEnv);
        return context;
    }
}
