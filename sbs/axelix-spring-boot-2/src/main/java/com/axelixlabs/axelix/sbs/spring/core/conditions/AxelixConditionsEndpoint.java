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
package com.axelixlabs.axelix.sbs.spring.core.conditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        ConditionsReportEndpoint.ApplicationConditionEvaluation original = delegate.applicationConditionEvaluation();

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

    private List<ConditionMatch> convertMatches(List<ConditionsReportEndpoint.MessageAndCondition> matches) {
        if (matches == null) {
            return Collections.emptyList();
        }
        return matches.stream()
                .map(m -> new ConditionMatch(m.getCondition(), m.getMessage()))
                .collect(Collectors.toList());
    }

    public static final class FlattenedConditionsDescriptor {
        private final List<PositiveCondition> positiveConditions;
        private final List<NegativeCondition> negativeConditions;

        public FlattenedConditionsDescriptor(
                List<PositiveCondition> positiveConditions, List<NegativeCondition> negativeConditions) {
            this.positiveConditions = positiveConditions;
            this.negativeConditions = negativeConditions;
        }

        public List<PositiveCondition> positiveConditions() {
            return positiveConditions;
        }

        public List<NegativeCondition> negativeConditions() {
            return negativeConditions;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (FlattenedConditionsDescriptor) obj;
            return Objects.equals(this.positiveConditions, that.positiveConditions)
                    && Objects.equals(this.negativeConditions, that.negativeConditions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(positiveConditions, negativeConditions);
        }

        @Override
        public String toString() {
            return "FlattenedConditionsDescriptor[" + "positiveConditions="
                    + positiveConditions + ", " + "negativeConditions="
                    + negativeConditions + ']';
        }
    }

    public static final class PositiveCondition {
        private final String target;
        private final List<ConditionMatch> matches;

        public PositiveCondition(String target, List<ConditionMatch> matches) {
            this.target = target;
            this.matches = matches;
        }

        public String target() {
            return target;
        }

        public List<ConditionMatch> matches() {
            return matches;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (PositiveCondition) obj;
            return Objects.equals(this.target, that.target) && Objects.equals(this.matches, that.matches);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, matches);
        }

        @Override
        public String toString() {
            return "PositiveCondition[" + "target=" + target + ", " + "matches=" + matches + ']';
        }
    }

    public static final class NegativeCondition {
        private final String target;
        private final List<ConditionMatch> notMatched;
        private final List<ConditionMatch> matched;

        public NegativeCondition(String target, List<ConditionMatch> notMatched, List<ConditionMatch> matched) {
            this.target = target;
            this.notMatched = notMatched;
            this.matched = matched;
        }

        public String target() {
            return target;
        }

        public List<ConditionMatch> notMatched() {
            return notMatched;
        }

        public List<ConditionMatch> matched() {
            return matched;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (NegativeCondition) obj;
            return Objects.equals(this.target, that.target)
                    && Objects.equals(this.notMatched, that.notMatched)
                    && Objects.equals(this.matched, that.matched);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, notMatched, matched);
        }

        @Override
        public String toString() {
            return "NegativeCondition[" + "target="
                    + target + ", " + "notMatched="
                    + notMatched + ", " + "matched="
                    + matched + ']';
        }
    }

    public static final class ConditionMatch {
        private final String condition;
        private final String message;

        public ConditionMatch(String condition, String message) {
            this.condition = condition;
            this.message = message;
        }

        public String condition() {
            return condition;
        }

        public String message() {
            return message;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ConditionMatch) obj;
            return Objects.equals(this.condition, that.condition) && Objects.equals(this.message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(condition, message);
        }

        @Override
        public String toString() {
            return "ConditionMatch[" + "condition=" + condition + ", " + "message=" + message + ']';
        }
    }
}
