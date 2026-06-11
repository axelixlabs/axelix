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
package com.axelixlabs.axelix.sbs.spring.core;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.beans.BeansEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.cache.CachesEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.conditions.ConditionsEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.details.DetailsEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvironmentEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvironmentTestConfig;
import com.axelixlabs.axelix.sbs.spring.core.gclog.GcEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.loggers.LoggersEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.master.MetadataEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.metrics.MetricsEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTasksEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.threaddump.ThreadDumpEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.utils.AxelixTestProperties;

/**
 * Base class for all the endpoint integration tests.
 *
 * <p>Every endpoint test extends this class and declares no context-affecting annotations of its
 * own (no {@code @SpringBootTest}, {@code @TestPropertySource}, {@code @Import} or nested
 * {@code @TestConfiguration} classes). As a result, all the endpoint tests produce an identical
 * merged context configuration and therefore share a single cached Spring application context,
 * which is only started once for the whole test run.
 *
 * <p>The annotations below are the union of the configuration previously declared by the
 * individual endpoint tests.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Artemiy Degtyarev
 */
@SpringBootTest(
        classes = Main.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = {"--axelix.env.test.prop3=fromCommandLine"},
        properties = {"axelix.env.test.prop2=systemValue2", "management.endpoints.web.exposure.include=*"})
@TestPropertySource(
        properties = {
            // beans endpoint
            "axelix.prop.test.name=axelix-beans",

            // conditions endpoint
            "axelix.conditions.test.flag=enabled",

            // environment endpoint
            "axelix.env.test.prop1=fromTestSource",
            "axelix.env.test.toBeSanitized=shouldBeSanitized",

            // loggers endpoint
            "logging.group.axelix.logger.group=axelix.logger.test",
            "logging.level.axelix.logger.test=WARN",
            "logging.level.a.b=WARN",
            "logging.level.a.b.c.d.e=DEBUG",

            // configprops/environment endpoints (bound into AxelixTestProperties)
            "axelix.prop.test.tags.forSanitization=toBeSanitized",
            "axelix.prop.test.tags.FOR_SANITIZATION=toBeSanitized",
            "axelix.prop.test.tags.environment=test",
            "axelix.prop.test.tags.version=1.0.0",
            "axelix.prop.test.enabled-contexts=user-service, payment-service",
            "axelix.prop.test.http-client.requests[0].name=user-api",
            "axelix.prop.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
            "axelix.prop.test.http-client.requests[0].methods[0].type=GET",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].count=3",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
            "axelix.prop.test.http-client.requests[0].methods[1].type=POST",
            "axelix.prop.test.http-client.requests[1].name=payment-api",
            "axelix.prop.test.http-client.requests[1].base-url=https://api.payments.example.com/v2",
            "axelix.prop.test.http-client.requests[1].methods[0].type=PUT",
            "axelix.prop.test.http-client.requests[1].methods[0].retries[0].count=2",
            "axelix.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level=DEBUG",
        })
@EnableConfigurationProperties(AxelixTestProperties.class)
@Import({
    EnvironmentTestConfig.class,
    EnvironmentEndpointTestConfiguration.class,
    ConfigurationPropertiesEndpointTestConfiguration.class,
    BeansEndpointTestConfiguration.class,
    CachesEndpointTestConfiguration.class,
    ConditionsEndpointTestConfiguration.class,
    DetailsEndpointTestConfiguration.class,
    GcEndpointTestConfiguration.class,
    LoggersEndpointTestConfiguration.class,
    MetadataEndpointTestConfiguration.class,
    MetricsEndpointTestConfiguration.class,
    ScheduledTasksEndpointTestConfiguration.class,
    ThreadDumpEndpointTestConfiguration.class,
    TransactionMonitoringEndpointTestConfiguration.class,
    JwtAuthTestConfiguration.class
})
public abstract class AbstractEndpointIntegrationTest {

    @DynamicPropertySource
    static void registerDynamic(DynamicPropertyRegistry registry) {
        registry.add("axelix.env.test.prop2", () -> "dynamicValue");
    }
}
