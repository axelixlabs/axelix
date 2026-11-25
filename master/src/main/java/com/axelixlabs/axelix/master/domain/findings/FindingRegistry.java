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
package com.axelixlabs.axelix.master.domain.findings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import com.axelixlabs.axelix.common.utils.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.master.domain.findings.AxelixFinding.FindingId;
import com.axelixlabs.axelix.master.domain.findings.PropertyFindingDiscoveryFunction.PropertyValue;
import com.axelixlabs.axelix.master.domain.findings.PropertyFindingDiscoveryFunction.Rule;

/**
 * Catalog of built-in property finding checks.
 */
public final class FindingRegistry {

    private static final List<PropertyFindingDiscoveryFunction> PROPERTY_CHECKS = load();

    private FindingRegistry() {}

    public static List<PropertyFindingDiscoveryFunction> getPropertyChecks(FindingTechnology technology) {
        return PROPERTY_CHECKS.stream()
                .filter(check -> check.responsibleFor().technology() == technology)
                .toList();
    }

    public static List<PropertyFinding> checkProperty(
            List<PropertyFindingDiscoveryFunction> checks, String normalizedName, @Nullable String value) {
        PropertyValue source = new PropertyValue(normalizedName, value);
        return checks.stream()
                .map(check -> check.check(source))
                .filter(Objects::nonNull)
                .toList();
    }

    private static List<PropertyFindingDiscoveryFunction> load() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("axelix-findings.yml"));
        Properties properties = Objects.requireNonNull(yaml.getObject());
        DefaultPropertyNameNormalizer normalizer = new DefaultPropertyNameNormalizer();
        return properties.stringPropertyNames().stream()
                .filter(key -> key.endsWith(".technology"))
                .sorted()
                .map(key -> {
                    String id = key.substring(0, key.length() - ".technology".length());
                    AxelixFinding finding = new AxelixFinding(
                            new FindingId(id),
                            FindingConcern.valueOf(properties.getProperty(id + ".concern")),
                            FindingTechnology.valueOf(properties.getProperty(key)));
                    List<Rule> rules = new ArrayList<>();
                    for (int index = 0; ; index++) {
                        String prefix = id + ".property-checks[" + index + "].";
                        String property = properties.getProperty(prefix + "property");
                        if (property == null) {
                            break;
                        }
                        rules.add(new Rule(
                                normalizer.normalize(property),
                                properties.getProperty(prefix + "value"),
                                Objects.requireNonNull(properties.getProperty(prefix + "rationale")),
                                Objects.requireNonNull(properties.getProperty(prefix + "alternative"))));
                    }
                    return new PropertyFindingDiscoveryFunction(finding, rules);
                })
                .toList();
    }
}
