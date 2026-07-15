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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.zaxxer.hikari.HikariConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.database.CommunityRDBMS;
import com.axelixlabs.axelix.master.repository.dialect.SQLiteDialect;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Autoconfiguration for {@link InstanceRegistry} persistence layer.
 *
 * @since 12.03.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class PersistenceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PersistenceAutoConfiguration.class);

    /**
     * Autoconfiguration for SQLite-based {@link InstanceRegistry}.
     */
    @AutoConfiguration
    @ConditionalOnCommunityRdbms(CommunityRDBMS.SQLITE)
    public static class SQLiteAutoConfiguration extends BaseJdbcConvertersAutoConfiguration {

        @Autowired
        private JsonMapper jsonMapper;

        @Bean
        @Override
        public @NonNull JdbcDialect jdbcDialect(@NonNull NamedParameterJdbcOperations operations) {
            return new SQLiteDialect();
        }

        @Bean
        @Primary
        public LiquibaseProperties sqliteLiquibaseProperties() {
            LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
            liquibaseProperties.setChangeLog("db/changelog/sqlite/db.changelog.master.sqlite.xml");
            return liquibaseProperties;
        }

        @Bean
        @Primary
        public HikariConfig sqliteHikariConfig() {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setMaximumPoolSize(3);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionInitSql(
                    "PRAGMA journal_mode=WAL; PRAGMA synchronous=NORMAL; PRAGMA foreign_keys=ON; PRAGMA busy_timeout=5000;");
            return hikariConfig;
        }

        @Override
        protected @NonNull List<?> userConverters() {
            List<?> superConverters = super.userConverters();

            List<?> thisConverters = List.of(
                    new InstantToStringConverter(),
                    new StringToInstantConverter(),
                    new LocalDateToStringConverter(),
                    new StringToLocalDateConverter(),
                    new IntegerToBooleanConverter());

            return Stream.concat(superConverters.stream(), thisConverters.stream())
                    .toList();
        }

        /**
         * Note that the reasons we have to convert {@link Instant} to a {@link String} for SQLite are:
         * <p>
         * 1. SQLite does not have a builtin TIMESTAMP/TIMESTAMPTZ/DATETIME data type or similar
         * 2. While SQLite has an INTEGER type, the JDBC driver often returns large numeric
         *    values as Strings, leading to conversion errors in Spring Data JDBC.
         */
        @WritingConverter
        public static class InstantToStringConverter implements Converter<Instant, String> {

            @Override
            public @NonNull String convert(@NonNull Instant source) {
                return String.valueOf(source.toEpochMilli());
            }
        }

        @ReadingConverter
        public static class StringToInstantConverter implements Converter<String, Instant> {

            @Override
            // NullAway is correct here, but Spring Framework Converter's contract is a bit wrong here
            @SuppressWarnings("NullAway")
            public @Nullable Instant convert(String source) {
                try {
                    long parsed = Long.parseLong(source);
                    return Instant.ofEpochMilli(parsed);
                } catch (NumberFormatException e) {
                    log.warn("Unable to parse the input '{}' as the numeric data type", source);
                    return null;
                }
            }
        }

        @WritingConverter
        public static class LocalDateToStringConverter implements Converter<LocalDate, String> {

            @Override
            public @NonNull String convert(@NonNull LocalDate source) {
                return String.valueOf(
                        source.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
        }

        @ReadingConverter
        public static class StringToLocalDateConverter implements Converter<String, LocalDate> {

            @Override
            // NullAway is correct here, but Spring Framework Converter's contract is a bit wrong here
            @SuppressWarnings("NullAway")
            public @Nullable LocalDate convert(String source) {
                try {
                    long parsed = Long.parseLong(source);
                    return Instant.ofEpochMilli(parsed)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                } catch (NumberFormatException e) {
                    return LocalDate.parse(source);
                }
            }
        }

        @ReadingConverter
        public static class IntegerToBooleanConverter implements Converter<Integer, Boolean> {

            @Override
            public @NonNull Boolean convert(@NonNull Integer source) {
                return source != 0;
            }
        }
    }

    @AutoConfiguration
    @ConditionalOnCommunityRdbms(CommunityRDBMS.POSTGRES)
    public static class PostgreSqlAutoConfiguration extends BaseJdbcConvertersAutoConfiguration {

        @Bean
        @Primary
        public LiquibaseProperties postgresLiquibaseProperties() {
            LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
            liquibaseProperties.setChangeLog("db/changelog/postgres/db.changelog.master.postgresql.xml");
            return liquibaseProperties;
        }

        @Override
        @NonNull
        public JdbcDialect jdbcDialect(@NonNull NamedParameterJdbcOperations operations) {
            return JdbcPostgresDialect.INSTANCE;
        }
    }

    @AutoConfiguration
    @ConditionalOnCommunityRdbms(CommunityRDBMS.MYSQL)
    public static class MySqlAutoConfiguration extends BaseJdbcConvertersAutoConfiguration {

        @Bean
        @Primary
        public LiquibaseProperties mysqlLiquibaseProperties() {
            LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
            liquibaseProperties.setChangeLog("db/changelog/mysql/db.changelog.master.mysql.xml");
            return liquibaseProperties;
        }
    }

    public static class BaseJdbcConvertersAutoConfiguration extends AbstractJdbcConfiguration {

        @Autowired
        protected JsonMapper jsonMapper;

        @Override
        protected @NonNull List<?> userConverters() {
            return List.of(
                    new RolesWritingConverter(jsonMapper),
                    new RolesReadingConverter(jsonMapper),
                    new PersistenceInsightsWritingConverter(jsonMapper),
                    new PersistenceInsightsReadingConverter(jsonMapper));
        }

        @WritingConverter
        public static class RolesWritingConverter implements Converter<UserEntity.Roles, String> {

            private final JsonMapper jsonMapper;

            public RolesWritingConverter(JsonMapper jsonMapper) {
                this.jsonMapper = jsonMapper;
            }

            @Override
            public String convert(UserEntity.Roles source) {
                return jsonMapper.writeValueAsString(source.values());
            }
        }

        @ReadingConverter
        public static class RolesReadingConverter implements Converter<String, UserEntity.Roles> {

            private final JsonMapper jsonMapper;

            public RolesReadingConverter(JsonMapper jsonMapper) {
                this.jsonMapper = jsonMapper;
            }

            @Override
            public UserEntity.Roles convert(String source) {
                if (source.isBlank()) {
                    return new UserEntity.Roles(Set.of());
                }
                Set<String> roles = jsonMapper.readValue(source, new TypeReference<>() {});
                return new UserEntity.Roles(roles);
            }
        }

        @WritingConverter
        public static class PersistenceInsightsWritingConverter implements Converter<PersistenceInsights, String> {

            private final JsonMapper jsonMapper;

            public PersistenceInsightsWritingConverter(JsonMapper jsonMapper) {
                this.jsonMapper = jsonMapper;
            }

            @Override
            public String convert(PersistenceInsights source) {
                return jsonMapper.writeValueAsString(source);
            }
        }

        @ReadingConverter
        public static class PersistenceInsightsReadingConverter implements Converter<String, PersistenceInsights> {

            private final JsonMapper jsonMapper;

            public PersistenceInsightsReadingConverter(JsonMapper jsonMapper) {
                this.jsonMapper = jsonMapper;
            }

            @Override
            public PersistenceInsights convert(String source) {
                if (source.isBlank()) {
                    return new PersistenceInsights(List.of());
                }
                return jsonMapper.readValue(source, PersistenceInsights.class);
            }
        }
    }
}
