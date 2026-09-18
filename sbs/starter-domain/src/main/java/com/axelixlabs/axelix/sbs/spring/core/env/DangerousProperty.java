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
package com.axelixlabs.axelix.sbs.spring.core.env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * The dictionary of the property values that we consider dangerous in production. One constant per
 * {@code (property, value)} pair, so a property with several offending values is listed several times.
 *
 * @author Sergey Cherkasov
 */
public enum DangerousProperty {
    OPEN_IN_VIEW(
            "spring.jpa.open-in-view",
            "true",
            "false",
            "Open Session In View keeps the persistence context open for the whole duration of the request, which "
                    + "hides the transaction boundaries, silently triggers lazy loading in the view layer and, as a "
                    + "consequence, leads to the N+1 problem and to the database connections being held much longer "
                    + "than necessary."),

    DDL_AUTO_CREATE(
            "spring.jpa.hibernate.ddl-auto",
            "create",
            "validate",
            "Hibernate recreates the whole schema on every startup, which means a guaranteed data loss. This is "
                    + "acceptable for a throwaway local setup, but never for an environment whose data matters."),

    DDL_AUTO_CREATE_DROP(
            "spring.jpa.hibernate.ddl-auto",
            "create-drop",
            "validate",
            "Hibernate creates the schema on startup and drops it on shutdown, which means a guaranteed data loss. "
                    + "This is acceptable for a throwaway local setup, but never for an environment whose data matters."),

    DDL_AUTO_UPDATE(
            "spring.jpa.hibernate.ddl-auto",
            "update",
            "validate",
            "Hibernate alters the schema implicitly at runtime, based on whatever the entity mapping happens to be. "
                    + "The resulting migration is neither reviewed nor reproducible, and Hibernate never drops or "
                    + "renames anything, so the schema silently drifts away from the mapping. A dedicated migration "
                    + "tool, such as Liquibase or Flyway, is the way to evolve a schema you care about."),

    SHOW_SQL(
            "spring.jpa.show-sql",
            "true",
            "false",
            "The generated SQL is printed straight to the standard output, bypassing the logging subsystem entirely, "
                    + "so it can be neither formatted, nor filtered, nor correlated with anything else. Raising the "
                    + "'org.hibernate.SQL' logger to DEBUG achieves the same goal without any of that."),

    ENABLE_LAZY_LOAD_NO_TRANS(
            "spring.jpa.properties.hibernate.enable_lazy_load_no_trans",
            "true",
            "false",
            "Every lazy association accessed outside of a transaction is loaded in a separate short-lived session of "
                    + "its own. This masks the missing transaction boundaries instead of exposing them, and turns a "
                    + "LazyInitializationException into a swarm of unnoticed queries."),

    INCLUDE_STACKTRACE_ALWAYS(
            "server.error.include-stacktrace",
            "always",
            "never",
            "The stack trace is included into every error response, which exposes the internals of the application, "
                    + "the libraries it is built upon and their versions to whoever is on the other side of the wire."),

    EXPOSURE_INCLUDE_ALL(
            "management.endpoints.web.exposure.include",
            "*",
            "health,info",
            "Every actuator endpoint present on the classpath is exposed over HTTP, including the ones that dump the "
                    + "heap, the thread stacks, the environment and the configuration properties. The endpoints that "
                    + "are actually needed are better listed explicitly.");

    private static final Map<String, List<DangerousProperty>> BY_NORMALIZED_NAME = buildIndex();

    private final String propertyName;
    private final String dangerousValue;
    private final @Nullable String alternativeExample;
    private final String rationale;

    DangerousProperty(
            String propertyName, String dangerousValue, @Nullable String alternativeExample, String rationale) {
        this.propertyName = propertyName;
        this.dangerousValue = dangerousValue;
        this.alternativeExample = alternativeExample;
        this.rationale = rationale;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getDangerousValue() {
        return dangerousValue;
    }

    public @Nullable String getAlternativeExample() {
        return alternativeExample;
    }

    public String getRationale() {
        return rationale;
    }

    /**
     * @param normalizedPropertyName the property name, already normalized by the {@link PropertyNameNormalizer}.
     * @param value                  the value the property is set to.
     * @return the matching constant, {@code null} when we consider the combination fine.
     */
    public static @Nullable DangerousProperty resolve(String normalizedPropertyName, @Nullable String value) {
        if (value == null) {
            return null;
        }

        List<DangerousProperty> candidates = BY_NORMALIZED_NAME.get(normalizedPropertyName);

        if (candidates == null) {
            return null;
        }

        String trimmedValue = value.trim();

        for (DangerousProperty candidate : candidates) {
            if (candidate.dangerousValue.equalsIgnoreCase(trimmedValue)) {
                return candidate;
            }
        }

        return null;
    }

    private static Map<String, List<DangerousProperty>> buildIndex() {
        PropertyNameNormalizer normalizer = new DefaultPropertyNameNormalizer();
        Map<String, List<DangerousProperty>> index = new HashMap<>();

        for (DangerousProperty dangerousProperty : values()) {
            index.computeIfAbsent(normalizer.normalize(dangerousProperty.propertyName), key -> new ArrayList<>())
                    .add(dangerousProperty);
        }

        return index;
    }
}
