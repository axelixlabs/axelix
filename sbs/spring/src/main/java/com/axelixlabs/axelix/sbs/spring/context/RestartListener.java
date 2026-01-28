/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.context;

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
