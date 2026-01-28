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
package com.axelixlabs.axelix.sbs.spring.conditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Custom endpoint to expose Conditions information.
 *
 * @author Nikita Kirilov
 * @author Mikhail Polivakha
 */
@RestControllerEndpoint(id = "axelix-conditions")
public class AxelixConditionsEndpoint {

    private final ConditionsReportEndpoint delegate;

    public AxelixConditionsEndpoint(ConfigurableApplicationContext configurableApplicationContext) {
        this.delegate = new ConditionsReportEndpoint(configurableApplicationContext);
    }

    @GetMapping
    public FlattenedConditionsDescriptor conditions() {
        ConditionsReportEndpoint.ConditionsDescriptor original = delegate.conditions();

        List<PositiveCondition> positiveConditions = new ArrayList<>();
        List<NegativeCondition> negativeConditions = new ArrayList<>();

        original.getContexts().values().forEach(context -> {
            if (context.getPositiveMatches() != null) {
                for (var entry : context.getPositiveMatches().entrySet()) {
                    positiveConditions.add(new PositiveCondition(entry.getKey(), convertMatches(entry.getValue())));
                }
            }

            if (context.getNegativeMatches() != null) {
                for (var entry : context.getNegativeMatches().entrySet()) {
                    negativeConditions.add(new NegativeCondition(
                            entry.getKey(),
                            convertMatches(entry.getValue().getNotMatched()),
                            convertMatches(entry.getValue().getMatched())));
                }
            }
        });

        return new FlattenedConditionsDescriptor(positiveConditions, negativeConditions);
    }

    private List<ConditionMatch> convertMatches(List<ConditionsReportEndpoint.MessageAndConditionDescriptor> matches) {
        if (matches == null) {
            return Collections.emptyList();
        }
        return matches.stream()
                .map(m -> new ConditionMatch(m.getCondition(), m.getMessage()))
                .toList();
    }

    public record FlattenedConditionsDescriptor(
            List<PositiveCondition> positiveConditions, List<NegativeCondition> negativeConditions) {}

    public record PositiveCondition(String target, List<ConditionMatch> matches) {}

    public record NegativeCondition(String target, List<ConditionMatch> notMatched, List<ConditionMatch> matched) {}

    public record ConditionMatch(String condition, String message) {}
}
