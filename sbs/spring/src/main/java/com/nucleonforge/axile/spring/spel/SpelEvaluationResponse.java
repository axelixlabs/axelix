package com.nucleonforge.axile.spring.spel;

import org.jspecify.annotations.Nullable;

/**
 * Result of evaluating a Spring Expression Language (SpEL) expression.
 * <p>
 * If {@code result} is {@code null}, it means the expression evaluated to {@code null}
 * (not that the evaluation failed). Evaluation errors should throw an exception instead
 * of creating this response.
 *
 * @param result string representation of the evaluation result, or {@code null} if the evaluated result is {@code null}
 *
 * @since 08.08.2025
 * author Nikita Kirillov
 */
public record SpelEvaluationResponse(@Nullable String result) {}
