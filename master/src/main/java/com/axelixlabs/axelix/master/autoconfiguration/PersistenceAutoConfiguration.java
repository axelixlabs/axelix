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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.zaxxer.hikari.HikariConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.axelixlabs.axelix.master.domain.UserEntity;
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
    @ConditionalOnClass(name = "org.sqlite.JDBC")
    @ConditionalOnJdbcUrlPrefix(jdbcUrlPrefix = "jdbc:sqlite:")
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
            return List.of(
                    new InstantToStringConverter(),
                    new StringToInstantConverter(),
                    new RolesWritingConverter(jsonMapper),
                    new RolesReadingConverter(jsonMapper));
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
    }

    @AutoConfiguration
    @ConditionalOnClass(name = "org.postgresql.Driver")
    @ConditionalOnJdbcUrlPrefix(jdbcUrlPrefix = "jdbc:postgresql:")
    public static class PostgreSqlAutoConfiguration extends BaseJdbcConvertersAutoConfiguration {

        @Bean
        @Primary
        public LiquibaseProperties postgresLiquibaseProperties() {
            LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
            liquibaseProperties.setChangeLog("db/changelog/postgres/db.changelog.master.postgresql.xml");
            return liquibaseProperties;
        }
    }

    @AutoConfiguration
    @ConditionalOnClass(name = "com.mysql.cj.jdbc.Driver")
    @ConditionalOnJdbcUrlPrefix(jdbcUrlPrefix = "jdbc:mysql:")
    public static class MySqlAutoConfiguration extends BaseJdbcConvertersAutoConfiguration {

        @Bean
        @Primary
        public LiquibaseProperties mysqlLiquibaseProperties() {
            LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
            liquibaseProperties.setChangeLog("db/changelog/mysql/db.changelog.master.mysql.xml");
            return liquibaseProperties;
        }
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Conditional(OnJdbcUrlCondition.class)
    public @interface ConditionalOnJdbcUrlPrefix {

        /**
         * @return JDBC url prefix
         */
        String jdbcUrlPrefix();
    }

    public static class OnJdbcUrlCondition extends SpringBootCondition {

        @Override
        public @NonNull ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String jdbcUrl = context.getEnvironment().getProperty("spring.datasource.url");
            MergedAnnotation<ConditionalOnJdbcUrlPrefix> annotation =
                    metadata.getAnnotations().get(ConditionalOnJdbcUrlPrefix.class);

            String expectedJdbcUrlPrefix = annotation.getString("jdbcUrlPrefix");

            if (jdbcUrl != null && jdbcUrl.startsWith(expectedJdbcUrlPrefix)) {
                return ConditionOutcome.match();
            } else {
                return ConditionOutcome.noMatch("jdbcUrlPrefix was expected to be '%s', but JDBC url actually is '%s'"
                        .formatted(expectedJdbcUrlPrefix, jdbcUrl));
            }
        }
    }

    public static class BaseJdbcConvertersAutoConfiguration extends AbstractJdbcConfiguration {

        @Autowired
        protected JsonMapper jsonMapper;

        @Override
        protected @NonNull List<?> userConverters() {
            return List.of(new RolesWritingConverter(jsonMapper), new RolesReadingConverter(jsonMapper));
        }

        @WritingConverter
        public static class RolesWritingConverter implements Converter<UserEntity.Roles, String> {

            private final JsonMapper jsonMapper;

            public RolesWritingConverter(JsonMapper jsonMapper) {
                this.jsonMapper = jsonMapper;
            }

            @Override
            public String convert(UserEntity.Roles source) {
                Set<String> roles = source == null ? Set.of() : source.values();
                return jsonMapper.writeValueAsString(roles);
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
                if (source == null || source.isBlank()) {
                    return new UserEntity.Roles(Set.of());
                }
                Set<String> roles = jsonMapper.readValue(source, new TypeReference<>() {});
                return new UserEntity.Roles(roles);
            }
        }
    }
}
