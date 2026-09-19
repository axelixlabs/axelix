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
package com.axelixlabs.axelix.master.domain.ecosystem.projects;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;

/**
 * The area of a running application a {@link SoftwareProject} focuses on. This is purely a presentation grouping.
 * As a result of this domain design, it is also assumed that every {@link Library} inside the {@link SoftwareProject}
 * logically belongs to the same {@link Ecosystem}. In practise it may not always be the case, but this is the decision for now.
 * <p>
 * A project is classified by what it does in the running application, regardless of who publishes it: Spring Security
 * belongs to {@link #SECURITY}, Spring Data to {@link #PERSISTENCE}, Spring Cloud Sleuth to {@link #OBSERVABILITY}.
 * There is deliberately no publisher- or portfolio-level constant here - that would be a second classification axis,
 * and every project of such a portfolio would then have two equally defensible homes.
 * <p>
 * At Axelix, we keep one manifest file per ecosystem, which is what keeps the classification consistent across entries.
 *
 * @author Mikhail Polivakha
 */
public enum Ecosystem {

    /**
     * ORMs, JDBC drivers, connection pools and schema migration tooling.
     */
    PERSISTENCE,

    /**
     * Object mapping and wire formats, e.g. Jackson, Gson, protobuf, SnakeYAML.
     */
    SERIALIZATION,

    /**
     * Logging facades, implementations and appenders.
     */
    LOGGING,

    /**
     * Metrics, tracing and profiling.
     */
    OBSERVABILITY,

    /**
     * Circuit breakers, retries, bulkheads and rate limiting.
     */
    RESILIENCE,

    /**
     * Authentication, authorization and cryptography.
     */
    SECURITY,

    /**
     * Brokers and messaging clients, e.g. Kafka, RabbitMQ, JMS.
     */
    MESSAGING,

    /**
     * Servlet containers, HTTP clients and web frameworks.
     */
    WEB,

    /**
     * Test frameworks, assertion libraries, mocking and test containers.
     */
    TESTING,

    /**
     * Anything that does not belong to one of the areas above, e.g. general purpose utility libraries.
     */
    OTHER
}
