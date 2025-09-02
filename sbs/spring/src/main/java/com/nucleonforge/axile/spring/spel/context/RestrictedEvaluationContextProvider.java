package com.nucleonforge.axile.spring.spel.context;

import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * A provider for creating {@link EvaluationContext} instances with restricted access.
 * <p>
 * Only beans explicitly listed in the configuration are available in the evaluation context.
 *
 * @author Nikita Kirillov
 * @since 12.08.2025
 */
public final class RestrictedEvaluationContextProvider implements EvaluationContextProvider {

    private final ApplicationContext applicationContext;
    private final Set<String> allowedBeanNames;
    private final Set<String> allowedClassNames;

    public RestrictedEvaluationContextProvider(
            ApplicationContext applicationContext, SpelEvaluationConfig spelEvaluationConfig) {
        this.applicationContext = applicationContext;
        this.allowedBeanNames = spelEvaluationConfig.getAllowedBeanNames();
        this.allowedClassNames = spelEvaluationConfig.getAllowedClassNames();
    }

    @Override
    public EvaluationContext initContext() {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

        SecureSpelContextConfigurator.configure(
                allowedBeanNames, allowedClassNames, applicationContext, evaluationContext);

        return evaluationContext;
    }
}
