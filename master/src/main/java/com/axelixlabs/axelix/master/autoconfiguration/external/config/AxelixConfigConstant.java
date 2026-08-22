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
package com.axelixlabs.axelix.master.autoconfiguration.external.config;

import java.util.List;

/**
 * Enumerates the configuration keys, values, and prefixes used to manage external configuration
 * infrastructure within Axelix Master.
 *
 * <p>Each constant encapsulates a semantic string {@link #value()} and categorizes it using a
 * specific {@link ConstantType}. These tokens decouple the core bootstrap logic in
 * {@link ExternalConfigurationEnvironmentPostProcessor} from hardcoded configuration strings,
 * streamlining properties mapping for Spring Cloud Config and upcoming providers.</p>
 *
 * @author Vyacheslav Yanin
 * @see ExternalConfigurationEnvironmentPostProcessor
 */
public enum AxelixConfigConstant {
    EXTERNAL_CONFIG_OPTION("axelix.master.external-config.option", ConstantType.PROPERTY),
    SPRING_CLOUD_CONFIG_VALUE("spring-cloud-config", ConstantType.VALUE),
    SPRING_CONFIG_IMPORT_PROPERTY("spring.config.import", ConstantType.PROPERTY),
    SPRING_CLOUD_CONFIG_ENABLED_PROPERTY("spring.cloud.config.enabled", ConstantType.PROPERTY),
    WAY_OF_CONFIG_IMPORT("configserver:", ConstantType.VALUE),

    AXELIX_PREFIX("axelix.master.external-config.spring-cloud-config.", ConstantType.PREFIX),
    SPRING_PREFIX("spring.cloud.config.", ConstantType.PREFIX),

    PROPERTY_SOURCE_NAME("axelixExternalConfigProperties", ConstantType.OTHER);

    private final String value;
    private final ConstantType type;

    AxelixConfigConstant(String value, ConstantType type) {
        this.value = value;
        this.type = type;
    }

    public String value() {
        return value;
    }

    public ConstantType type() {
        return type;
    }

    public enum ConstantType {
        PROPERTY,
        VALUE,
        PREFIX,
        OTHER
    }

    public static final List<String> MAPPED_PROPERTIES = List.of("uri", "label", "name", "username", "password");
}
