package com.nucleonforge.axile.sbs.spring.context;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The object that contains all the information necessary to stop and restart context later without loss of state.
 *
 * @since 04.07.25
 * @author Mikhail Polivakha
 */
public class ApplicationSnapshotEvent extends SpringApplicationEvent {

    private final ConfigurableApplicationContext context;

    /**
     * Create a new {@link ApplicationSnapshotEvent} instance.
     *
     * @param application the current application
     * @param args the arguments the application is running with
     * @param context the ApplicationContext about to be refreshed
     */
    public ApplicationSnapshotEvent(
            ConfigurableApplicationContext context, SpringApplication application, String[] args) {
        super(application, args);
        this.context = context;
    }

    /**
     * Return the application context.
     * @return the context
     */
    public ConfigurableApplicationContext getApplicationContext() {
        return this.context;
    }
}
