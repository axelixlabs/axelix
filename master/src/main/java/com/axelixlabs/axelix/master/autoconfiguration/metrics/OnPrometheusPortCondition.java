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

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.axelixlabs.axelix.master.autoconfiguration.metrics.ConditionalOnPrometheusPort.Mode;

import static com.axelixlabs.axelix.master.autoconfiguration.metrics.PrometheusProperties.PROMETHEUS_PORT_PROPERTY;
import static com.axelixlabs.axelix.master.autoconfiguration.metrics.PrometheusProperties.SERVER_PORT_PROPERTY;

/**
 * Condition backing {@link ConditionalOnPrometheusPort}: matches when whether
 * {@code axelix.master.metrics.prometheus.port} equals {@code server.port} agrees with the
 * annotation's {@link Mode}. Activates {@link PrometheusMetricsAutoConfiguration}'s
 * {@code prometheusEndpoint} bean when the ports match, and its {@code prometheusHttpServer} bean
 * when they differ.
 *
 * @author Dmitry Mazurov
 */
public class OnPrometheusPortCondition extends SpringBootCondition {

    // Environment does not expose server.port when it is left unset - Boot's embedded web server
    // falls back to this port on its own, so mirror that default here.
    private static final int DEFAULT_SERVER_PORT = 8080;

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        MergedAnnotation<ConditionalOnPrometheusPort> annotation =
                metadata.getAnnotations().get(ConditionalOnPrometheusPort.class);

        boolean expectedMatch = Mode.MATCHES_APPLICATION_PORT.equals(annotation.getEnum("value", Mode.class));
        boolean portsMatch = matchesServerPort(context.getEnvironment());

        if (portsMatch == expectedMatch) {
            return ConditionOutcome.match();
        }

        return ConditionOutcome.noMatch(
                expectedMatch
                        ? "Prometheus is exposed through a dedicated HTTP server instead"
                        : "Prometheus is exposed through the actuator instead");
    }

    /**
     * Checks whether {@code axelix.master.metrics.prometheus.port} is not set, or is explicitly
     * set and equals {@code server.port}.
     *
     * @param properties the environment to read {@code server.port} and
     *     {@code axelix.master.metrics.prometheus.port} from
     * @return {@code true} if the ports match, {@code false} otherwise
     */
    private static boolean matchesServerPort(PropertyResolver properties) {
        int serverPort = properties.getProperty(SERVER_PORT_PROPERTY, Integer.class, DEFAULT_SERVER_PORT);
        Integer prometheusPort = properties.getProperty(PROMETHEUS_PORT_PROPERTY, Integer.class);

        return prometheusPort == null || serverPort == prometheusPort;
    }
}
