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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

/**
 * Resolves the Spring Cloud release train version from the Spring Cloud Commons module version.
 *
 * @author Sergey Cherkasov
 */
final class SpringCloudVersionResolver {

    private static final String SPRING_CLOUD_COMMONS_CLASS_NAME =
            "org.springframework.cloud.client.CommonsClientAutoConfiguration";

    private static final String RELEASE_TRAINS_MAPPING_RESOURCE =
            "META-INF/axelix/spring-cloud-release-trains.properties";

    private static final Map<String, String> RELEASE_TRAIN_BY_COMMONS_VERSION = loadReleaseTrainMappings();

    private SpringCloudVersionResolver() {}

    static @Nullable String resolve() {
        Class<?> springCloudCommonsClass = loadSpringCloudCommonsClass();
        if (springCloudCommonsClass == null) {
            return null;
        }

        Package springCloudCommonsPackage = springCloudCommonsClass.getPackage();
        if (springCloudCommonsPackage == null) {
            return null;
        }

        return resolveFromSpringCloudCommonsVersion(springCloudCommonsPackage.getImplementationVersion());
    }

    static @Nullable String resolveFromSpringCloudCommonsVersion(@Nullable String springCloudCommonsVersion) {
        if (springCloudCommonsVersion == null) {
            return null;
        }

        return RELEASE_TRAIN_BY_COMMONS_VERSION.get(springCloudCommonsVersion);
    }

    private static @Nullable Class<?> loadSpringCloudCommonsClass() {
        Class<?> clazz = loadClass(
                SPRING_CLOUD_COMMONS_CLASS_NAME, Thread.currentThread().getContextClassLoader());
        if (clazz != null) {
            return clazz;
        }

        return loadClass(SPRING_CLOUD_COMMONS_CLASS_NAME, SpringCloudVersionResolver.class.getClassLoader());
    }

    private static @Nullable Class<?> loadClass(String className, @Nullable ClassLoader classLoader) {
        if (classLoader == null) {
            return null;
        }

        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Map<String, String> loadReleaseTrainMappings() {
        ClassLoader classLoader = SpringCloudVersionResolver.class.getClassLoader();
        if (classLoader == null) {
            return Map.of();
        }

        try (InputStream inputStream = classLoader.getResourceAsStream(RELEASE_TRAINS_MAPPING_RESOURCE)) {
            if (inputStream == null) {
                return Map.of();
            }

            Properties properties = new Properties();
            properties.load(inputStream);

            Map<String, String> mappings = new HashMap<>();
            for (String springCloudCommonsVersion : properties.stringPropertyNames()) {
                String releaseTrainVersion = properties.getProperty(springCloudCommonsVersion);
                if (releaseTrainVersion != null) {
                    mappings.put(springCloudCommonsVersion, releaseTrainVersion);
                }
            }

            return Map.copyOf(mappings);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load Spring Cloud release train mappings", e);
        }
    }
}
