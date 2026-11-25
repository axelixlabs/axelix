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
package com.axelixlabs.axelix.master.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.utils.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.common.utils.PropertyNameNormalizer;

/**
 * The dictionary of the property values that we consider dangerous in production. One constant per
 * {@code (property, value)} pair, so a property with several offending values is listed several times.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public enum DangerousPropertyValue {
    OPEN_IN_VIEW(
            "spring.jpa.open-in-view",
            Set.of(
                    new DangerousValue(
                            "true",
                            "Open Session In View keeps the persistence context open for the whole duration of the request, which "
                                    + "hides the transaction boundaries, silently triggers lazy loading in the view layer and, as a "
                                    + "consequence, leads to the N+1 problem and to the database connections being held much longer "
                                    + "than necessary.",
                            "false"),
                    new DangerousValue(
                            null,
                            "Open Session In View keeps the persistence context open for the whole duration of the request, which "
                                    + "hides the transaction boundaries, silently triggers lazy loading in the view layer and, as a "
                                    + "consequence, leads to the N+1 problem and to the database connections being held much longer "
                                    + "than necessary.",
                            "false"))),

    DDL_AUTO_CREATE(
            "spring.jpa.hibernate.ddl-auto",
            Set.of(
                    new DangerousValue(
                            "create",
                            "Hibernate recreates the whole schema on every startup, which means a guaranteed data loss. This is "
                                    + "acceptable for a throwaway local setup, but never for an environment whose data matters.",
                            "validate"),
                    new DangerousValue(
                            "create-drop",
                            "Hibernate creates the schema on startup and drops it on shutdown, which means a guaranteed data loss. "
                                    + "This is acceptable for a throwaway local setup, but never for an environment whose data matters.",
                            "validate"),
                    new DangerousValue(
                            "update",
                            "Hibernate alters the schema implicitly at runtime, based on whatever the entity mapping happens to be. "
                                    + "The resulting migration is neither reviewed nor reproducible, and Hibernate never drops or "
                                    + "renames anything, so the schema silently drifts away from the mapping. A dedicated migration "
                                    + "tool, such as Liquibase or Flyway, is the way to evolve a schema you care about.",
                            "validate"))),

    LIQUIBASE_DROP_FIRST(
            "spring.liquibase.drop-first",
            Set.of(new DangerousValue(
                    "true",
                    "Liquibase drops existing database objects before running migrations at startup. "
                            + "If the application points to a production database, a restart can erase tables and their data.",
                    "false"))),

    FLYWAY_CLEAN_ENABLED(
            "spring.flyway.clean-disabled",
            Set.of(new DangerousValue(
                    "false",
                    "This permits Flyway's clean operation to run against the configured schemas. "
                            + "An accidental clean against production can delete tables and their data.",
                    "true"))),

    H2_CONSOLE_ENABLED(
            "spring.h2.console.enabled",
            Set.of(new DangerousValue(
                    "true",
                    "This enables the H2 development console over HTTP. "
                            + "Anyone who can reach the console and sign in can run SQL to read, change, or delete database data.",
                    "false"))),

    SHOW_SQL(
            "spring.jpa.show-sql",
            Set.of(new DangerousValue(
                    "true",
                    "The generated SQL is printed straight to the standard output, bypassing the logging subsystem entirely, "
                            + "so it can be neither formatted, nor filtered, nor correlated with anything else. Raising the "
                            + "'org.hibernate.SQL' logger to DEBUG achieves the same goal without any of that.",
                    "false"))),

    ENABLE_LAZY_LOAD_NO_TRANS(
            "spring.jpa.properties.hibernate.enable_lazy_load_no_trans",
            Set.of(new DangerousValue(
                    "true",
                    "Every lazy association accessed outside of a transaction is loaded in a separate short-lived session of "
                            + "its own. This masks the missing transaction boundaries instead of exposing them, and turns a "
                            + "LazyInitializationException into a swarm of unnoticed queries.",
                    "false"))),

    INCLUDE_STACKTRACE_ALWAYS(
            "server.error.include-stacktrace",
            Set.of(
                    new DangerousValue(
                            "always",
                            "The stack trace is included into every error response, which exposes the internals of the application, "
                                    + "the libraries it is built upon and their versions to whoever is on the other side of the wire.",
                            "never"),
                    new DangerousValue(
                            "on-param",
                            "Clients can ask Spring Boot to include stack traces in error responses. "
                                    + "Those traces reveal internal class names and library details that help callers map the application.",
                            "never"))),

    INCLUDE_MESSAGE_ALWAYS(
            "server.error.include-message",
            Set.of(
                    new DangerousValue(
                            "always",
                            "Spring Boot includes internal error messages in HTTP error responses when available. "
                                    + "Those messages can reveal file paths, SQL statements, or database details to callers.",
                            "never"),
                    new DangerousValue(
                            "on-param",
                            "Clients can ask Spring Boot to include internal error messages in HTTP error responses. "
                                    + "Those messages can reveal file paths, SQL statements, or database details.",
                            "never"))),

    INCLUDE_BINDING_ERRORS_ALWAYS(
            "server.error.include-binding-errors",
            Set.of(
                    new DangerousValue(
                            "always",
                            "Spring Boot includes binding and validation errors in HTTP error responses when available. "
                                    + "This can reveal internal field names and echo rejected input, including sensitive values.",
                            "never"),
                    new DangerousValue(
                            "on-param",
                            "Clients can ask Spring Boot to include binding and validation errors in HTTP error responses. "
                                    + "Those errors can reveal internal field names or echo sensitive input.",
                            "never"))),

    WEB_INCLUDE_STACKTRACE_ALWAYS(
            "spring.web.error.include-stacktrace",
            Set.of(
                    new DangerousValue(
                            "always",
                            "Spring Boot includes stack traces in error responses whenever a trace is available. "
                                    + "These traces reveal internal class and method names and which libraries the application uses.",
                            "never"),
                    new DangerousValue(
                            "on-param",
                            "Clients can ask Spring Boot to include stack traces in error responses. "
                                    + "Those traces reveal internal class names and library details that help callers map the application.",
                            "never"))),

    WEB_INCLUDE_MESSAGE_ALWAYS(
            "spring.web.error.include-message",
            Set.of(
                    new DangerousValue(
                            "always",
                            "Spring Boot includes internal error messages in HTTP error responses when available. "
                                    + "Those messages can reveal file paths, SQL statements, or database details to callers.",
                            "never"),
                    new DangerousValue(
                            "on-param",
                            "Clients can ask Spring Boot to include internal error messages in HTTP error responses. "
                                    + "Those messages can reveal file paths, SQL statements, or database details.",
                            "never"))),

    WEB_INCLUDE_BINDING_ERRORS_ALWAYS(
            "spring.web.error.include-binding-errors",
            Set.of(
                    new DangerousValue(
                            "always",
                            "Spring Boot includes binding and validation errors in HTTP error responses when available. "
                                    + "This can reveal internal field names and echo rejected input, including sensitive values.",
                            "never"),
                    new DangerousValue(
                            "on-param",
                            "Clients can ask Spring Boot to include binding and validation errors in HTTP error responses. "
                                    + "Those errors can reveal internal field names or echo sensitive input.",
                            "never"))),

    IMMEDIATE_SHUTDOWN(
            "server.shutdown",
            Set.of(new DangerousValue(
                    "immediate",
                    "Immediate shutdown can interrupt in-flight HTTP requests during restarts and deployments. "
                            + "Graceful shutdown gives active requests time to complete.",
                    "graceful"))),

    ENV_SHOW_VALUES_ALWAYS(
            "management.endpoint.env.show-values",
            Set.of(new DangerousValue(
                    "always",
                    "The Actuator env endpoint can return environment values without masking them. "
                            + "Anyone with endpoint access could see credentials or API tokens stored in environment properties. "
                            + "Use the Axelix UI or MCP server to inspect environment properties and keep the standard "
                            + "Actuator env endpoint unexposed.",
                    "never"))),

    CONFIGPROPS_SHOW_VALUES_ALWAYS(
            "management.endpoint.configprops.show-values",
            Set.of(new DangerousValue(
                    "always",
                    "The Actuator configprops endpoint can return configuration values without masking them. "
                            + "Anyone with endpoint access could see passwords or tokens bound to configuration beans. "
                            + "Use the Axelix UI or MCP server to inspect configuration properties and keep the standard "
                            + "Actuator configprops endpoint unexposed.",
                    "never"))),

    EXPOSURE_INCLUDE_ALL(
            "management.endpoints.web.exposure.include",
            Set.of(new DangerousValue(
                    "*",
                    "Every actuator endpoint present on the classpath is exposed over HTTP, including the ones that dump the "
                            + "heap, the thread stacks, the environment and the configuration properties. The endpoints that "
                            + "are actually needed are better listed explicitly.",
                    "health,info")));

    private static final Map<String, DangerousPropertyValue> BY_NORMALIZED_NAME = buildIndex();

    private final String propertyName;
    private final Set<DangerousValue> dangerousValues;

    DangerousPropertyValue(String propertyName, Set<DangerousValue> dangerousValues) {
        this.propertyName = propertyName;
        this.dangerousValues = dangerousValues;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Set<DangerousValue> getDangerousValues() {
        return dangerousValues;
    }

    /**
     * @param normalizedPropertyName the property name, already normalized by the {@link PropertyNameNormalizer}.
     * @param value                  the value the property is set to.
     * @return the matching constant, {@code null} when we consider the combination fine.
     */
    public static @Nullable DangerousValue resolve(String normalizedPropertyName, @Nullable String value) {
        DangerousPropertyValue candidate = BY_NORMALIZED_NAME.get(normalizedPropertyName);

        if (candidate == null) {
            return null;
        }

        for (DangerousValue dangerousValue : candidate.getDangerousValues()) {
            boolean matches = dangerousValue.value() == null
                    ? value == null
                    : dangerousValue.value().equalsIgnoreCase(value);
            if (matches) {
                return dangerousValue;
            }
        }

        return null;
    }

    private static Map<String, DangerousPropertyValue> buildIndex() {
        PropertyNameNormalizer normalizer = new DefaultPropertyNameNormalizer();
        Map<String, DangerousPropertyValue> index = new HashMap<>();

        for (DangerousPropertyValue dangerousProperty : values()) {
            index.computeIfAbsent(normalizer.normalize(dangerousProperty.propertyName), _ -> dangerousProperty);
        }

        return index;
    }

    /**
     * @param value the value considered dangerous, matched without regard to case. May be {@code null}
     *              when null itself is dangerous.
     * @param rationale the rationale behind why this is dangerous
     * @param alternative the alternative value to be used instead
     */
    public record DangerousValue(@Nullable String value, String rationale, String alternative) {}
}
