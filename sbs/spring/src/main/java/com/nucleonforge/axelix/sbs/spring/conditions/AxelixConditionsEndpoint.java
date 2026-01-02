/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.conditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;

/**
 * Custom endpoint to expose Conditions information.
 *
 * @author Nikita Kirilov
 * @author Mikhail Polivakha
 */
@RestControllerEndpoint(id = "axelix-conditions")
public class AxelixConditionsEndpoint {

    private final ConditionsReportEndpoint delegate;

    public AxelixConditionsEndpoint(ConditionsReportEndpoint delegate) {
        this.delegate = delegate;
    }

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
