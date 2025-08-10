package com.nucleonforge.axile.spring.spel;

/**
 * Represents a request to evaluate a Spring Expression Language (SpEL) expression.
 *
 * @param spelExpression the SpEL expression to be evaluated
 *
 * @since 12.08.2025
 * @author Nikita Kirillov
 */
public record SpelEvaluationRequest(String spelExpression) {}
