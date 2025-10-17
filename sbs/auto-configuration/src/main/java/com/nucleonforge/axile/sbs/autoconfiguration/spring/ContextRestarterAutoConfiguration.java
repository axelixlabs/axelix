package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.context.ContextRestarter;
import com.nucleonforge.axile.sbs.spring.context.DefaultContextRestarter;
import com.nucleonforge.axile.sbs.spring.context.RestartListener;

/**
 * Auto-configuration for context restart support.
 *
 * <p>This configuration registers beans that handle application context restart events.
 * It provides a {@link ContextRestarter} bean responsible for triggering context restarts,
 * and a {@link RestartListener} bean that listens for restart events.</p>
 *
 * @since 10.07.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class ContextRestarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ContextRestarter contextRestarter() {
        return new DefaultContextRestarter();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestartListener restartListener() {
        return new RestartListener();
    }
}
