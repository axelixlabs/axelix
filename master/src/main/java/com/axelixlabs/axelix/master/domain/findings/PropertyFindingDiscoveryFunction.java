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

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.findings.PropertyFindingDiscoveryFunction.PropertyValue;

/**
 * Checks a property against the configured values for one finding.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public final class PropertyFindingDiscoveryFunction
        implements FindingDiscoveryFunction<PropertyValue, PropertyFinding> {

    private final AxelixFinding finding;
    private final List<Rule> rules;

    PropertyFindingDiscoveryFunction(AxelixFinding finding, List<Rule> rules) {
        this.finding = finding;
        this.rules = List.copyOf(rules);
    }

    @Override
    public @Nullable PropertyFinding check(@Nullable PropertyValue source) {
        if (source == null) {
            return null;
        }
        for (Rule rule : rules) {
            if (rule.normalizedName().equals(source.normalizedName())
                    && (rule.value() == null
                            ? source.value() == null
                            : rule.value().equalsIgnoreCase(source.value()))) {
                return new PropertyFinding(finding, rule.rationale(), rule.alternative());
            }
        }
        return null;
    }

    @Override
    public AxelixFinding responsibleFor() {
        return finding;
    }

    public record PropertyValue(
            String normalizedName, @Nullable String value) {}

    record Rule(String normalizedName, @Nullable String value, String rationale, String alternative) {}
}
