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
package com.axelixlabs.axelix.master.autoconfiguration.metrics;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * Conditional annotation to activate configuration depending on how
 * {@code axelix.master.metrics.prometheus.port} relates to {@code server.port}: with
 * {@link PrometheusPortMode#MATCHES_APPLICATION_PORT}, activates when the ports match (Prometheus
 * exposed through the actuator); with {@link PrometheusPortMode#CUSTOM_PORT_CONFIGURED}, activates
 * when they differ (Prometheus exposed through a dedicated HTTP server).
 *
 * @author Dmitry Mazurov
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnPrometheusPortCondition.class)
public @interface ConditionalOnPrometheusPort {

    /**
     * Which port relationship this condition should match.
     */
    PrometheusPortMode value();
}
