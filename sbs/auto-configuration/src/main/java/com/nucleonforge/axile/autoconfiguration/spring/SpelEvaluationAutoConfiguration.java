package com.nucleonforge.axile.autoconfiguration.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.spring.spel.DefaultSpelEvaluator;
import com.nucleonforge.axile.spring.spel.SpelEvalEndpoint;
import com.nucleonforge.axile.spring.spel.SpelEvaluator;
import com.nucleonforge.axile.spring.spel.context.EvaluationContextProvider;
import com.nucleonforge.axile.spring.spel.context.RestrictedEvaluationContextProvider;
import com.nucleonforge.axile.spring.spel.context.SpelEvaluationConfig;

/**
 * Auto-configuration for SpEL (Spring Expression Language) evaluation infrastructure.
 *
 * <p><b>Activation:</b> Requires explicit enable through property:
 * {@code axile.sbs.spel.enabled=true}
 *
 * <p>Configuration properties are available under {@code axile.sbs.spel} prefix.
 */
@AutoConfiguration
@EnableConfigurationProperties(SpelEvaluationConfig.class)
@ConditionalOnProperty(name = "axile.sbs.spel.enabled", havingValue = "true")
public class SpelEvaluationAutoConfiguration {

    @Bean
    public EvaluationContextProvider evaluationContextFactory(
            ApplicationContext applicationContext, SpelEvaluationConfig evaluationConfig) {
        return new RestrictedEvaluationContextProvider(applicationContext, evaluationConfig);
    }

    @Bean
    public SpelEvaluator spelEvaluator(EvaluationContextProvider evaluationContextProvider) {
        return new DefaultSpelEvaluator(evaluationContextProvider);
    }

    @Bean
    public SpelEvalEndpoint spelEvalEndpoint(SpelEvaluator spelEvaluator) {
        return new SpelEvalEndpoint(spelEvaluator);
    }
}
