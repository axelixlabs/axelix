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
package com.axelixlabs.axelix.common.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The flattened version of conditions response from the actuator endpoint.
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 */
public final class ConditionsFeed {

    private final List<PositiveCondition> positiveConditions;
    private final List<NegativeCondition> negativeConditions;

    @JsonCreator
    public ConditionsFeed(
            @JsonProperty("positiveConditions") List<PositiveCondition> positiveConditions,
            @JsonProperty("negativeConditions") List<NegativeCondition> negativeConditions) {
        this.positiveConditions = positiveConditions != null ? positiveConditions : Collections.emptyList();
        this.negativeConditions = negativeConditions != null ? negativeConditions : Collections.emptyList();
    }

    public List<PositiveCondition> getPositiveConditions() {
        return positiveConditions;
    }

    public List<NegativeCondition> getNegativeConditions() {
        return negativeConditions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConditionsFeed that = (ConditionsFeed) o;
        return Objects.equals(positiveConditions, that.positiveConditions)
                && Objects.equals(negativeConditions, that.negativeConditions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positiveConditions, negativeConditions);
    }

    @Override
    public String toString() {
        return "ConditionsFeed{"
                + "positiveConditions="
                + positiveConditions
                + ", negativeConditions="
                + negativeConditions
                + '}';
    }

    public static final class PositiveCondition {

        private final String target;
        private final List<ConditionMatch> matches;

        @JsonCreator
        public PositiveCondition(
                @JsonProperty("target") String target, @JsonProperty("matches") List<ConditionMatch> matches) {
            this.target = target;
            this.matches = matches != null ? matches : Collections.emptyList();
        }

        public String getTarget() {
            return target;
        }

        public List<ConditionMatch> getMatches() {
            return matches;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PositiveCondition that = (PositiveCondition) o;
            return Objects.equals(target, that.target) && Objects.equals(matches, that.matches);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, matches);
        }

        @Override
        public String toString() {
            return "PositiveCondition{" + "target='" + target + '\'' + ", matches=" + matches + '}';
        }
    }

    public static final class NegativeCondition {

        private final String target;
        private final List<ConditionMatch> notMatched;
        private final List<ConditionMatch> matched;

        @JsonCreator
        public NegativeCondition(
                @JsonProperty("target") String target,
                @JsonProperty("notMatched") List<ConditionMatch> notMatched,
                @JsonProperty("matched") List<ConditionMatch> matched) {
            this.target = target;
            this.notMatched = notMatched != null ? notMatched : Collections.emptyList();
            this.matched = matched != null ? matched : Collections.emptyList();
        }

        public String getTarget() {
            return target;
        }

        public List<ConditionMatch> getNotMatched() {
            return notMatched;
        }

        public List<ConditionMatch> getMatched() {
            return matched;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NegativeCondition that = (NegativeCondition) o;
            return Objects.equals(target, that.target)
                    && Objects.equals(notMatched, that.notMatched)
                    && Objects.equals(matched, that.matched);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, notMatched, matched);
        }

        @Override
        public String toString() {
            return "NegativeCondition{"
                    + "target='"
                    + target
                    + '\''
                    + ", notMatched="
                    + notMatched
                    + ", matched="
                    + matched
                    + '}';
        }
    }

    public static final class ConditionMatch {

        private final String condition;
        private final String message;

        @JsonCreator
        public ConditionMatch(@JsonProperty("condition") String condition, @JsonProperty("message") String message) {
            this.condition = condition;
            this.message = message;
        }

        public String getCondition() {
            return condition;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ConditionMatch that = (ConditionMatch) o;
            return Objects.equals(condition, that.condition) && Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(condition, message);
        }

        @Override
        public String toString() {
            return "ConditionMatch{" + "condition='" + condition + '\'' + ", message='" + message + '\'' + '}';
        }
    }
}
