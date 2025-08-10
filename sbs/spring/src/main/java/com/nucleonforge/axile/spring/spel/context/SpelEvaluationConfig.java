package com.nucleonforge.axile.spring.spel.context;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/**
 * Configuration properties for SpEL (Spring Expression Language) evaluation restrictions.
 *
 * <p>Defines security constraints for SpEL expressions by specifying whitelists of accessible
 * beans and classes.
 */
@ConfigurationProperties(prefix = "axile.sbs.spel")
public final class SpelEvaluationConfig {

    /**
     * Names of beans explicitly allowed for SpEL expressions.
     */
    private final Set<String> allowedBeanNames;

    /**
     * Fully-qualified class names explicitly allowed for type references in SpEL.
     */
    private final Set<String> allowedClassNames;

    @ConstructorBinding
    public SpelEvaluationConfig(Set<String> allowedBeanNames, Set<String> allowedClassNames) {
        this.allowedBeanNames = allowedBeanNames;
        this.allowedClassNames = allowedClassNames;
    }

    public Set<String> getAllowedBeanNames() {
        return allowedBeanNames;
    }

    public Set<String> getAllowedClassNames() {
        return allowedClassNames;
    }
}
