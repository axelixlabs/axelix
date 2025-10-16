package com.nucleonforge.axile.sbs.spring.context;

import org.springframework.context.ApplicationContext;

/**
 * Implementations of this interface are capable to restart the entire Spring's {@link ApplicationContext}.
 *
 * @since 04.07.25
 * @author Mikhail Polivakha
 */
public interface ContextRestarter {

    void restartContext();
}
