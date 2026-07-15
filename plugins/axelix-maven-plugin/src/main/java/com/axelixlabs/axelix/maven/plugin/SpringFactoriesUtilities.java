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
package com.axelixlabs.axelix.maven.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with {@code spring.factories} files
 *
 * @author Artemiy Degtyarev
 */
@Named("springFactoriesUtilities")
@Singleton
public class SpringFactoriesUtilities {
    private static final Logger log = LoggerFactory.getLogger(SpringFactoriesUtilities.class);

    /**
     * Convert {@code spring.factories} content from {@link Map} to {@link Properties}
     * @param content {@code spring.factories} file content
     * @return content converted to {@link Properties}
     */
    public Properties convertToProperties(Map<String, Set<String>> content) {
        Properties properties = new Properties();
        content.forEach((k, v) -> properties.put(k, String.join(",", v)));

        return properties;
    }

    /**
     * Merges separate {@code spring.factories} to one
     * @param properties List of {@code spring.factories} files content
     * @return Merged {@code spring.factories} configuration
     */
    public Map<String, Set<String>> merge(List<Map<String, Set<String>>> properties) {
        Map<String, Set<String>> result = new HashMap<>();

        for (Map<String, Set<String>> map : properties) {
            map.forEach((k, v) -> result.merge(k, v, (cur, incoming) -> {
                HashSet<String> merged = new HashSet<>(cur);
                merged.addAll(incoming);

                return merged;
            }));
        }

        return result;
    }

    /**
     * Loads {@code spring.factories} to map
     *
     * @param path Path of {@code spring.factories} file
     * @return Map of properties
     */
    public Map<String, Set<String>> load(String path) {
        Properties properties;
        try {
            properties = loadProperties(path);
        } catch (IOException e) {
            log.error("Failed to load spring.factories file in: {}", path);
            throw new RuntimeException(e);
        }

        Map<String, Set<String>> result = new HashMap<>();
        properties.forEach((name, value) -> {
            String[] classNames = ((String) value).split(",");
            Set<String> classes =
                    result.computeIfAbsent(((String) name).trim(), key -> new HashSet<>(classNames.length));
            Arrays.stream(classNames).map(String::trim).forEach(classes::add);
        });

        return result;
    }

    private static Properties loadProperties(String path) throws IOException {
        try (InputStream stream = Files.newInputStream(Path.of(path))) {
            Properties properties = new Properties();
            properties.load(stream);

            return properties;
        }
    }
}
