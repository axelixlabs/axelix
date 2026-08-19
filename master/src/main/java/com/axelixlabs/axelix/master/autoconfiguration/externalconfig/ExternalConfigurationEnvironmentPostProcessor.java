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
package com.axelixlabs.axelix.master.autoconfiguration.externalconfig;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Dynamically sets {@code spring.config.import} based on the list of external
 * configuration sources specified in {@code axelix.master.external-config.options}.
 *
 * <p>The processor runs before {@link ConfigDataEnvironmentPostProcessor}, which means
 * the options must be provided via environment variables or system properties — not
 * from {@code application.yaml}, since YAML files are not yet loaded at this stage.
 *
 * @author Ilya Naumov
 */
public class ExternalConfigurationEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    public static final String SPRING_CONFIG_IMPORT_PROPERTY = "spring.config.import";
    public static final String AXELIX_MASTER_EXTERNAL_CONFIG_OPTIONS = "axelix.master.external-config.options";

    private static final String PROPERTY_SOURCE_NAME = "axelixExternalConfig";

    /**
     * Runs before {@link ConfigDataEnvironmentPostProcessor}.
     *
     * @return one less than {@link ConfigDataEnvironmentPostProcessor#ORDER}, ensuring
     * this processor runs before YAML-based config data is loaded
     */
    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER - 1;
    }

    /**
     * Binds the external config options from the environment, resolves the corresponding
     * import prefixes and required properties, and adds them as a high-priority property source.
     *
     * <p>For each enabled option it:
     * <ol>
     *   <li>Resolves the corresponding {@code spring.config.import} prefix (e.g. {@code vault://}).</li>
     *   <li>Enables required properties (e.g. {@code spring.cloud.vault.enabled}).</li>
     *   <li>Adds all resolved imports and properties as a high-priority property source.</li>
     * </ol>
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        List<ExternalConfigOption> options;
        try {
            options = Binder.get(environment)
                    .bind(AXELIX_MASTER_EXTERNAL_CONFIG_OPTIONS, Bindable.listOf(ExternalConfigOption.class))
                    .orElseGet(List::of)
                    .stream()
                    .distinct()
                    .sorted(Comparator.comparingInt(ExternalConfigOption::getOrder))
                    .toList();
        } catch (BindException e) {
            throw new IllegalStateException("Invalid value for '" + AXELIX_MASTER_EXTERNAL_CONFIG_OPTIONS + "': '"
                    + environment.getProperty(AXELIX_MASTER_EXTERNAL_CONFIG_OPTIONS) + "'. Supported values are: "
                    + Stream.of(ExternalConfigOption.values())
                            .map(ExternalConfigOption::toString)
                            .collect(Collectors.joining(", ")));
        }

        if (options.isEmpty()) {
            return;
        }

        List<String> imports =
                options.stream().map(ExternalConfigOption::getImportLocation).toList();

        Map<String, Object> properties = options.stream()
                .flatMap(option -> option.getProperties().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        properties.put(SPRING_CONFIG_IMPORT_PROPERTY, String.join(",", imports));
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }
}
