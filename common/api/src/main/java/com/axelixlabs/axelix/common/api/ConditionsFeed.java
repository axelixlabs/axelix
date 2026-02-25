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
import org.jspecify.annotations.Nullable;

/**
 * The flattened version of conditions response from the actuator endpoint.
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public final class ConditionsFeed {

    private final List<PositiveCondition> positiveMatches;
    private final List<NegativeCondition> negativeMatches;

    /**
     * Create new ConditionsFeed.
     *
     * @param positiveMatches list of configuration classes where all conditions matched successfully
     * @param negativeMatches list of configuration classes where some conditions did not match
     */
    @JsonCreator
    public ConditionsFeed(
            @JsonProperty("positiveMatches") List<PositiveCondition> positiveMatches,
            @JsonProperty("negativeMatches") List<NegativeCondition> negativeMatches) {
        this.positiveMatches = positiveMatches != null ? positiveMatches : Collections.emptyList();
        this.negativeMatches = negativeMatches != null ? negativeMatches : Collections.emptyList();
    }

    public List<PositiveCondition> getPositiveMatches() {
        return positiveMatches;
    }

    public List<NegativeCondition> getNegativeMatches() {
        return negativeMatches;
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
        return Objects.equals(positiveMatches, that.positiveMatches)
                && Objects.equals(negativeMatches, that.negativeMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positiveMatches, negativeMatches);
    }

    @Override
    public String toString() {
        return "ConditionsFeed{" + "positiveMatches=" + positiveMatches + ", negativeMatches=" + negativeMatches + '}';
    }

    /**
     * Represents a configuration class where all conditions matched successfully.
     */
    public static final class PositiveCondition {

        private final String className;

        @Nullable
        private final String methodName;

        private final List<ConditionMatch> matched;

        /**
         * Create new PositiveCondition
         *
         * @param className  the class name of the class on which either the conditional annotation resided, or
         *                   that contained the {@link #methodName} on which the conditional annotation resided.
         *                   Guaranteed to be present.
         * @param methodName the name of the method on which the conditional annotation was put.
         * @param matched    list of conditions that were evaluated and matched
         */
        @JsonCreator
        public PositiveCondition(
                @JsonProperty("className") String className,
                @JsonProperty("methodName") @Nullable String methodName,
                @JsonProperty("matched") List<ConditionMatch> matched) {
            this.className = className;
            this.methodName = methodName;
            this.matched = matched;
        }

        public String getClassName() {
            return className;
        }

        public @Nullable String getMethodName() {
            return methodName;
        }

        public List<ConditionMatch> getMatched() {
            return matched;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PositiveCondition that = (PositiveCondition) o;
            return Objects.equals(className, that.className)
                    && Objects.equals(methodName, that.methodName)
                    && Objects.equals(matched, that.matched);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, methodName, matched);
        }

        @Override
        public String toString() {
            return "PositiveCondition{" + "className='"
                    + className + '\'' + ", methodName='"
                    + methodName + '\'' + ", matched="
                    + matched + '}';
        }
    }

    /**
     * Represents a configuration class where some conditions did not match.
     */
    public static final class NegativeCondition {

        private final String className;

        @Nullable
        private final String methodName;

        private final List<ConditionMatch> notMatched;
        private final List<ConditionMatch> matched;

        /**
         * Create new NegativeCondition.
         *
         * @param className  the class name of the class on which either the conditional annotation resided, or
         *                   that contained the {@link #methodName} on which the conditional annotation resided.
         *                   Guaranteed to be present.
         * @param methodName the name of the method on which the conditional annotation was put.
         * @param notMatched list of conditions that were not matched
         * @param matched list of conditions that were matched
         */
        @JsonCreator
        public NegativeCondition(
                @JsonProperty("className") String className,
                @JsonProperty("methodName") @Nullable String methodName,
                @JsonProperty("notMatched") List<ConditionMatch> notMatched,
                @JsonProperty("matched") List<ConditionMatch> matched) {
            this.className = className;
            this.methodName = methodName;
            this.notMatched = notMatched;
            this.matched = matched;
        }

        public String getClassName() {
            return className;
        }

        public @Nullable String getMethodName() {
            return methodName;
        }

        public List<ConditionMatch> getNotMatched() {
            return notMatched;
        }

        public List<ConditionMatch> getMatched() {
            return matched;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NegativeCondition that = (NegativeCondition) o;
            return Objects.equals(className, that.className)
                    && Objects.equals(methodName, that.methodName)
                    && Objects.equals(notMatched, that.notMatched)
                    && Objects.equals(matched, that.matched);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, methodName, notMatched, matched);
        }

        @Override
        public String toString() {
            return "NegativeCondition{" + "className='"
                    + className + '\'' + ", methodName='"
                    + methodName + '\'' + ", notMatched="
                    + notMatched + ", matched="
                    + matched + '}';
        }
    }

    /**
     * Represents the result of evaluating a single condition.
     */
    public static final class ConditionMatch {

        private final String condition;
        private final String message;

        /**
         * Create new ConditionMatch.
         *
         * @param condition the simple name of the condition class that was evaluated
         * @param message descriptive message explaining why the condition matched or did not match
         */
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
