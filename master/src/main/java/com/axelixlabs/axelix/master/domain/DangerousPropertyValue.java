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
            Set.of(new DangerousValue(
                    "true",
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
            Set.of(new DangerousValue(
                    "always",
                    "The stack trace is included into every error response, which exposes the internals of the application, "
                            + "the libraries it is built upon and their versions to whoever is on the other side of the wire.",
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
            if (value == null) {
                if (dangerousValue.value() == null) {
                    return dangerousValue;
                }
            } else {
                if (value.equalsIgnoreCase(dangerousValue.value())) {
                    return dangerousValue;
                }
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
     * @param value the actual value considered dangerous. Might be {@code null} in case the null
     *              is the actually dangerous value.
     * @param rationale the rationale behind why is this dangerous
     * @param alternative the alternative value to be used instead
     */
    public record DangerousValue(@Nullable String value, String rationale, String alternative) {}
}
