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
package com.axelixlabs.axelix.sbs.spring.core.env;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.sbs.spring.core.Main;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;

/**
 * Base class for the {@code env} integration tests, including {@link AxelixEnvironmentEndpoint}. Every subclass
 * inherits the same merged test configuration, so Spring's TestContext cache builds the context once and reuses it
 * across all of them. The {@link TestPropertySource} below is the union of the properties each test needs; the keys
 * do not collide and {@code args} is harmless to tests that do not read it.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Artemiy Degtyarev
 */
@SpringBootTest(
        classes = Main.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = {"--fooBar=fromArgs", "--axelix.env.test.prop3=fromCommandLine"},
        properties = {
            "axelix.env.test.prop2=systemValue2",
            "management.endpoint.env.show-values=always",
        })
@TestPropertySource(
        properties = {
            // DefaultPropertyMetadataExtractorTest
            "prop.test.server.port=test",
            "prop.test.logging.level.root=test",
            "custom.test.without.reason.property=test",
            "custom.test.without.replacement.property=test",
            // ValueInjectionTrackerBeanPostProcessorTest
            "test.server.port=9090",
            "test.spring.application.name=TimeoutTestApp",
            "test.spring.profiles.active=production",
            "test.app.timeout=3000",
            "test.inner.timeout=1500",
            "test.inner.constructor.timeout=2500",
            "test.method.timeout=4200",
            // DefaultEnvironmentServiceTest (explicit sanitization scenario)
            "axelix.prop.test.tags.forSanitization=toBeSanitized",
            "axelix.prop.test.tags.FOR_SANITIZATION=toBeSanitized",
            // AxelixEnvironmentEndpointTest
            "axelix.env.test.prop1=fromTestSource",
            "axelix.env.test.toBeSanitized=shouldBeSanitized",
            "axelix.prop.test.tags.environment=test",
            "axelix.prop.test.tags.version=1.0.0",
            "axelix.prop.test.enabled-contexts=user-service,payment-service",
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
@EnableConfigurationProperties(EnvSharedTestConfig.AxelixConfigurationProperties.class)
@Import({EnvSharedTestConfig.class, JwtAuthTestConfiguration.class})
abstract class AbstractEnvSharedContextTest {

    @DynamicPropertySource
    static void registerDynamic(DynamicPropertyRegistry registry) {
        registry.add("axelix.env.test.prop2", () -> "dynamicValue");
    }
}
