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
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for the {@code env} integration tests (the endpoint test is intentionally excluded). Every subclass
 * inherits the same merged test configuration, so Spring's TestContext cache builds the context once and reuses it
 * across all of them. The {@link TestPropertySource} below is the union of the properties each test needs; the keys
 * do not collide and {@code args} is harmless to tests that do not read it.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Artemiy Degtyarev
 */
@SpringBootTest(args = "--fooBar=fromArgs")
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
            "axelix.prop.test.tags.FOR_SANITIZATION=toBeSanitized"
        })
@EnableConfigurationProperties(EnvSharedTestConfig.AxelixConfigurationProperties.class)
@Import(EnvSharedTestConfig.class)
abstract class AbstractEnvSharedContextTest {}
