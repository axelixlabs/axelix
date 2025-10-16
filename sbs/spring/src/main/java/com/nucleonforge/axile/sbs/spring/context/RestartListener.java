package com.nucleonforge.axile.sbs.spring.context;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;

/**
 * Listener that listens for Context being refreshed related events to store the information necessary for
 * later context reload.
 *
 * @implNote Most of this code is kindly borrowed from Spring Cloud Common in order to avoid introducing Spring Cloud dependency.
 * @author Mikhail Polivakha
 */
public class RestartListener implements SmartApplicationListener {

    @Nullable
    private ConfigurableApplicationContext context;

    @Nullable
    private ApplicationSnapshotEvent snapshot;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean supportsEventType(@NonNull Class<? extends ApplicationEvent> eventType) {
        return ApplicationPreparedEvent.class.isAssignableFrom(eventType)
                || ContextRefreshedEvent.class.isAssignableFrom(eventType)
                || ContextClosedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent input) {
        if (input instanceof ApplicationPreparedEvent applicationPreparedEvent) {
            this.snapshot = new ApplicationSnapshotEvent(
                    applicationPreparedEvent.getApplicationContext(),
                    applicationPreparedEvent.getSpringApplication(),
                    applicationPreparedEvent.getArgs());
            if (this.context == null) {
                this.context = this.snapshot.getApplicationContext();
            }
        } else if (input instanceof ContextRefreshedEvent) {
            if (this.context != null && input.getSource().equals(this.context) && this.snapshot != null) {
                this.context.publishEvent(this.snapshot);
            }
        } else {
            if (this.context != null && input.getSource().equals(this.context)) {
                this.context = null;
                this.snapshot = null;
            }
        }
    }
}
