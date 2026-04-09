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

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.repository.dialect.SQLiteDialect;

/**
 * Autoconfiguration for {@link com.axelixlabs.axelix.master.service.state.InstanceRegistry} persistence layer.
 *
 * @since 12.03.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class PersistenceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PersistenceAutoConfiguration.class);

    /**
     * Autoconfiguration for SQLite-based {@link com.axelixlabs.axelix.master.service.state.InstanceRegistry}.
     */
    @AutoConfiguration
    @ConditionalOnClass(name = "org.sqlite.JDBC")
    @Conditional(SQLiteJdbcUrlCondition.class)
    public static class SQLiteAutoConfiguration extends BaseJdbcConvertersAutoConfiguration {

        @Autowired
        private JsonMapper jsonMapper;

        @Bean
        @Override
        public @NonNull JdbcDialect jdbcDialect(@NonNull NamedParameterJdbcOperations operations) {
            return new SQLiteDialect();
        }

        @Override
        protected @NonNull List<?> userConverters() {
            return List.of(
                    new InstantToStringConverter(),
                    new StringToInstantConverter(),
                    new VmFeaturesReadingConverter(jsonMapper),
                    new VmFeaturesWritingConverter(jsonMapper));
        }
    }

    public static class SQLiteJdbcUrlCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            String jdbcUrl = context.getEnvironment().getProperty("spring.datasource.url");
            return jdbcUrl != null && jdbcUrl.startsWith("jdbc:sqlite:");
        }
    }

    /**
     * Autoconfiguration for PostgreSQL-based {@link com.axelixlabs.axelix.master.service.state.InstanceRegistry}.
     */
    @AutoConfiguration
    @ConditionalOnClass(name = "org.postgresql.Driver")
    @Conditional(PostgreSqlJdbcUrlCondition.class)
    public static class PostgreSqlAutoConfiguration extends BaseJdbcConvertersAutoConfiguration {}

    public static class PostgreSqlJdbcUrlCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            String jdbcUrl = context.getEnvironment().getProperty("spring.datasource.url");
            return jdbcUrl != null && jdbcUrl.startsWith("jdbc:postgresql:");
        }
    }

    /**
     * Autoconfiguration for MySQL-based {@link com.axelixlabs.axelix.master.service.state.InstanceRegistry}.
     */
    @AutoConfiguration
    @ConditionalOnClass(name = "com.mysql.cj.jdbc.Driver")
    @Conditional(MySqlJdbcUrlCondition.class)
    public static class MySqlAutoConfiguration extends BaseJdbcConvertersAutoConfiguration {}

    public static class MySqlJdbcUrlCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            String jdbcUrl = context.getEnvironment().getProperty("spring.datasource.url");
            return jdbcUrl != null && jdbcUrl.startsWith("jdbc:mysql:");
        }
    }

    public static class BaseJdbcConvertersAutoConfiguration extends AbstractJdbcConfiguration {

        @Autowired
        protected JsonMapper jsonMapper;

        @Override
        protected @NonNull List<?> userConverters() {
            return List.of(new VmFeaturesReadingConverter(jsonMapper), new VmFeaturesWritingConverter(jsonMapper));
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
        public static class VmFeaturesWritingConverter implements Converter<Instance.VmFeatures, String> {

            private final JsonMapper jsonMapper;

            public VmFeaturesWritingConverter(JsonMapper jsonMapper) {
                this.jsonMapper = jsonMapper;
            }

            @Override
            public String convert(Instance.VmFeatures source) {
                Set<Instance.VMFeature> features = source == null ? Set.of() : source.features();
                return jsonMapper.writeValueAsString(features);
            }
        }

        @ReadingConverter
        public static class VmFeaturesReadingConverter implements Converter<String, Instance.VmFeatures> {

            private final JsonMapper jsonMapper;

            public VmFeaturesReadingConverter(JsonMapper jsonMapper) {
                this.jsonMapper = jsonMapper;
            }

            @Override
            public Instance.VmFeatures convert(String source) {
                if (source == null || source.isBlank()) {
                    return Instance.VmFeatures.empty();
                }
                Set<Instance.VMFeature> features = jsonMapper.readValue(source, new TypeReference<>() {});
                return Instance.VmFeatures.of(features);
            }
        }
    }
}
