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
package com.axelixlabs.axelix.sbs.spring.core.shared;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.axelixlabs.axelix.sbs.spring.core.Main;

/**
 * Common base for all endpoint integration tests.
 *
 * <p>All concrete {@code *EndpointTest} subclasses use the exact same
 * effective Spring configuration (same {@code @SpringBootTest},
 * same {@code @Import}, same {@code args}, same dynamic property sources).
 * This is intentional: the Spring TestContext framework caches application
 * contexts by their effective configuration, so all subclasses share a
 * single {@code ApplicationContext} that is built once for the entire
 * endpoint-test suite.
 *
 * @since 14.05.2026
 * @author Artemiy Degtyarev
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Main.class,
        args = {"--axelix.env.test.prop3=fromCommandLine", "--fooBar=fromArgs"},
        // The actuator-published {@code ConfigurationPropertiesReportEndpoint} (used directly by
        // {@code DefaultConfigurationPropertiesConverterTest}) defaults to {@code WHEN_AUTHORIZED} for value
        // visibility, which redacts every value when no security context is bound. Axelix's own
        // {@code axelix-configprops} path goes through {@link
        // com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesService}, which builds
        // its own delegate endpoints and is unaffected by this property, so no other endpoint test changes.
        properties = "management.endpoint.configprops.show-values=always")
@Import(SharedEndpointTestConfiguration.class)
public abstract class AbstractEndpointTest {

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        // {@code DynamicPropertyRegistry} contributes a property source that ranks above
        // system properties. We use it to set the {@code prop2} value the env endpoint test
        // expects, and to lift {@code prop1} above the {@code @BeforeEach} system properties
        // that the same test injects.
        registry.add("axelix.env.test.prop1", () -> "fromTestSource");
        registry.add("axelix.env.test.prop2", () -> "dynamicValue");

        // {@code tags.forSanitization} / {@code tags.FOR_SANITIZATION} are registered here
        // rather than in {@code application.yaml} so that non-endpoint tests (which use the
        // same {@code application.yaml}) are not accidentally affected by them.
        registry.add("axelix.prop.test.tags.forSanitization", () -> "toBeSanitized");
        registry.add("axelix.prop.test.tags.FOR_SANITIZATION", () -> "toBeSanitized");

        // Required by the {@link com.axelixlabs.axelix.sbs.spring.core.env.TestBeanWithCustomAnnotations}
        // bean (registered for {@code ValueInjectionTrackerBeanPostProcessorTest}). Its {@code @Autowired
        // setProfile} setter and {@code calculateRandomTimeout} method use placeholders without defaults,
        // so the bean fails to construct unless these resolve.
        registry.add("test.spring.profiles.active", () -> "production");
        registry.add("test.method.timeout", () -> "4200");

        // Required by {@link com.axelixlabs.axelix.sbs.spring.core.env.DefaultPropertyMetadataExtractorTest}. The
        // metadata extractor only keeps entries for property names actually present in the {@link
        // org.springframework.core.env.Environment} (see {@code DefaultPropertyMetadataExtractor#filterMetadata}),
        // so these keys must exist for the test's lookups to succeed. Descriptions/deprecations come from
        // {@code spring-configuration-metadata.json} on the test classpath.
        registry.add("prop.test.server.port", () -> "test");
        registry.add("prop.test.logging.level.root", () -> "test");
        registry.add("custom.test.without.reason.property", () -> "test");
        registry.add("custom.test.without.replacement.property", () -> "test");
    }
}
