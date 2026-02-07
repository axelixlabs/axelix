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
package com.axelixlabs.axelix.master.api.external.response;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * The feed of the conditions in the application.
 *
 * @param positiveMatches list of configuration classes where all conditions matched successfully
 * @param negativeMatches list of configuration classes where some conditions did not match
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 */
public record ConditionsFeedResponse(List<PositiveCondition> positiveMatches, List<NegativeCondition> negativeMatches) {

    public ConditionsFeedResponse {
        if (positiveMatches == null) {
            positiveMatches = Collections.emptyList();
        }
        if (negativeMatches == null) {
            negativeMatches = Collections.emptyList();
        }
    }

    /**
     * Represents a configuration class where all conditions matched successfully.
     *
     * @param className  the class name of the class on which either the conditional annotation resided, or
     *                   that contained the {@link #methodName} on which the conditional annotation resided.
     *                   Guaranteed to be present.
     * @param methodName the name of the method on which the conditional annotation was put.
     * @param matched    list of conditions that were evaluated and matched
     */
    public record PositiveCondition(String className, @Nullable String methodName, List<ConditionMatch> matched) {

        public PositiveCondition {
            if (matched == null) {
                matched = Collections.emptyList();
            }
        }
    }

    /**
     * Represents a configuration class where some conditions did not match.
     *
     * @param className  the class name of the class on which either the conditional annotation resided, or
     *                   that contained the {@link #methodName} on which the conditional annotation resided.
     *                   Guaranteed to be present.
     * @param methodName the name of the method on which the conditional annotation was put.
     * @param notMatched list of conditions that were not matched
     * @param matched list of conditions that were matched
     */
    public record NegativeCondition(
            String className,
            @Nullable String methodName,
            List<ConditionMatch> notMatched,
            List<ConditionMatch> matched) {

        public NegativeCondition {
            if (notMatched == null) {
                notMatched = Collections.emptyList();
            }
            if (matched == null) {
                matched = Collections.emptyList();
            }
        }
    }

    /**
     * Represents the result of evaluating a single condition.
     *
     * @param condition the simple name of the condition class that was evaluated
     * @param message descriptive message explaining why the condition matched or did not match
     */
    public record ConditionMatch(String condition, String message) {}
}
