package com.nucleonforge.axile.master.api.response;

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
