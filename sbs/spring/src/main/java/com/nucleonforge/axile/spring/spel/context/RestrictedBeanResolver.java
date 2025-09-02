package com.nucleonforge.axile.spring.spel.context;

import java.util.Set;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.AccessException;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.EvaluationContext;

/**
 * A {@link BeanResolver} implementation that restricts access to a specific set of beans.
 * <p>
 * Only beans explicitly listed in {@code allowedBeans} can be resolved. Any attempt to access
 * a bean outside this set, or a bean that does not exist in the {@link ApplicationContext},
 * will result in an {@link AccessException}.
 *
 * @since 13.08.2025
 * @author Nikita Kirillov
 */
public final class RestrictedBeanResolver implements BeanResolver {

    private final ApplicationContext applicationContext;
    private final Set<String> allowedBeans;

    public RestrictedBeanResolver(ApplicationContext applicationContext, Set<String> allowedBeans) {
        this.applicationContext = applicationContext;
        this.allowedBeans = allowedBeans;
    }

    @Override
    public Object resolve(@NonNull EvaluationContext context, @NonNull String beanName) throws AccessException {
        if (!allowedBeans.contains(beanName)) {
            throw new AccessException("Access to bean '" + beanName
                    + "' is denied. Only beans listed in the application properties/yaml are allowed.");
        }

        if (!applicationContext.containsBean(beanName)) {
            throw new AccessException("Bean '" + beanName + "' not found in application context");
        }

        try {
            return applicationContext.getBean(beanName);
        } catch (BeansException ex) {
            throw new AccessException("Failed to resolve bean '" + beanName + "'", ex);
        }
    }
}
