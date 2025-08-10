package com.nucleonforge.axile.spring.spel.context;

import org.springframework.expression.EvaluationContext;

/**
 * Provider interface for creating and initializing SpEL evaluation contexts.
 *
 * @since 12.08.2025
 * @author Nikita Kirillov
 */
public sealed interface EvaluationContextProvider permits RestrictedEvaluationContextProvider {

    /**
     * Creates and initializes a new {@link EvaluationContext} instance.
     * <p>
     * Implementations are responsible for creating, populating the context with necessary beans,
     * variables, and configuration before it is used for evaluating SpEL expressions.
     *
     * @return a fully configured {@link EvaluationContext}
     */
    EvaluationContext initContext();
}
