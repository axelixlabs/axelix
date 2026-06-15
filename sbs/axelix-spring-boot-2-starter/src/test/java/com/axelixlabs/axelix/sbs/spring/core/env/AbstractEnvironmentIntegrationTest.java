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

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for the non-endpoint tests of the {@code env} package.
 *
 * <p>Every such test extends this class and declares no context-affecting annotations of its own
 * (no {@code @SpringBootTest}, {@code @TestPropertySource}, {@code @Import} or nested
 * {@code @TestConfiguration} classes). As a result, all of them produce an identical merged
 * context configuration and therefore share a single cached Spring application context. The
 * property list is the union of the properties required by each participating test, and the
 * imports register the beans required by all of them.
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

            // DefaultEnvironmentServiceTest.WithExplicitSanitizationProperties
            "axelix.prop.test.tags.forSanitization=toBeSanitized",
            "axelix.prop.test.tags.FOR_SANITIZATION=toBeSanitized"
        })
@Import({
    EnvironmentTestConfig.class,
    EnvironmentSharedTestConfiguration.class,
    ValueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations.class,
    ValueInjectionTrackerBeanPostProcessorTest.TestBeanWithSpEL.class
})
public abstract class AbstractEnvironmentIntegrationTest {}
