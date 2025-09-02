package com.nucleonforge.axile.spring.spel;

/**
 * The interface defines a contract for evaluating Spring Expression Language (SpEL) expressions.
 *
 * @since 08.08.2025
 * @author Nikita Kirillov
 */
public interface SpelEvaluator {

    /**
     * Evaluates the given SpEL expression and returns the result.
     *
     * @param spelExpression the SpEL expression to evaluate
     * @return the evaluation result wrapped in {@link SpelEvaluationResponse}
     * @throws SpelException if the expression is invalid or evaluation fails
     */
    SpelEvaluationResponse evaluate(String spelExpression) throws SpelException;
}
