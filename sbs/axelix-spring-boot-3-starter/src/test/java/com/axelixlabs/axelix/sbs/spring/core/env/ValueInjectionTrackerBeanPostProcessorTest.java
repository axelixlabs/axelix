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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionPoint;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ValueInjectionTrackerBeanPostProcessor}. The analyzed sample beans live in
 * {@link EnvSharedTestConfig}, so the tracked injection points carry the bean name generated for those
 * {@code @Bean} methods.
 *
 * @since 16.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
class ValueInjectionTrackerBeanPostProcessorTest extends AbstractEnvSharedContextTest {

    private static final String SAMPLE_BEAN_NAME = "testBeanWithCustomAnnotations";

    @Autowired
    private ValueInjectionTrackerBeanPostProcessor subject;

    private final PropertyNameNormalizer normalizer = new DefaultPropertyNameNormalizer();

    @Test
    void testDirectValueInjectionOnField() {
        // given.
        String propertyServerPort = "test.server.port";

        // when.
        List<InjectionPoint> points = subject.getInjectionPointsForProperty(normalizer.normalize(propertyServerPort));

        // then.
        assertThat(points).hasSize(1).first().satisfies(point -> {
            assertThat(point.getBeanName()).isEqualTo(SAMPLE_BEAN_NAME);
            assertThat(point.getInjectionType()).isEqualTo(InjectionType.FIELD);
            assertThat(point.getTargetName()).isEqualTo("serverPort");
            assertThat(point.getPropertyExpression()).isEqualTo("${" + propertyServerPort + ":8080}");
        });
    }

    @Test
    void testMetaAnnotationValueInjectionOnField() {
        // given.
        String propertyTimeout = "test.app.timeout";

        // when.
        List<InjectionPoint> points = subject.getInjectionPointsForProperty(normalizer.normalize(propertyTimeout));
        InjectionPoint injectionPoint = points.stream()
                .filter(point -> point.getInjectionType().equals(InjectionType.FIELD)
                        && point.getTargetName().equals("timeout"))
                .findAny()
                .orElseThrow();

        // then.
        assertThat(injectionPoint.getBeanName()).isEqualTo(SAMPLE_BEAN_NAME);
        assertThat(injectionPoint.getPropertyExpression()).isEqualTo("${" + propertyTimeout + ":5000}");
    }

    @Test
    void testDirectConstructorParameterInjection() {
        // when
        String propertyApplicationName = "test.spring.application.name";

        // then.
        List<InjectionPoint> appNamePoints =
                subject.getInjectionPointsForProperty(normalizer.normalize(propertyApplicationName));
        assertThat(appNamePoints).hasSize(1).first().satisfies(point -> {
            assertThat(point.getBeanName()).isEqualTo(SAMPLE_BEAN_NAME);
            assertThat(point.getInjectionType()).isEqualTo(InjectionType.CONSTRUCTOR_PARAMETER);
            assertThat(point.getTargetName()).isEqualTo("appName");
            assertThat(point.getPropertyExpression()).isEqualTo("${" + propertyApplicationName + ":TestApp}");
        });
    }

    @Test
    void testMetaAnnotationValueOnConstructorParameterInjection() {
        // when.
        List<InjectionPoint> timeoutPoints =
                subject.getInjectionPointsForProperty(normalizer.normalize("test.app.timeout"));

        // then.
        assertThat(timeoutPoints)
                .filteredOn(point -> point.getInjectionType() == InjectionType.CONSTRUCTOR_PARAMETER
                        && point.getTargetName().equals("connectionTimeout"))
                .hasSize(1)
                .first()
                .satisfies(point -> {
                    assertThat(point.getBeanName()).isEqualTo(SAMPLE_BEAN_NAME);
                    assertThat(point.getPropertyExpression()).isEqualTo("${test.app.timeout:5000}");
                });
    }

    @Test
    void testDirectMethodParameterInjection() {
        // when.
        String propertyProfile = "test.spring.profiles.active";

        // then.
        List<InjectionPoint> profilePoints =
                subject.getInjectionPointsForProperty(normalizer.normalize(propertyProfile));
        assertThat(profilePoints).hasSize(1).first().satisfies(point -> {
            assertThat(point.getBeanName()).isEqualTo(SAMPLE_BEAN_NAME);
            assertThat(point.getInjectionType()).isEqualTo(InjectionType.METHOD_PARAMETER);
            assertThat(point.getTargetName()).contains("setProfile");
            assertThat(point.getPropertyExpression()).isEqualTo("${" + propertyProfile + "}");
        });
    }

    @Test
    void testMetaAnnotationOnMethodParameterInjection() {
        // when.
        String propertyTimeout = "test.app.timeout";

        // then.
        List<InjectionPoint> timeoutPoints =
                subject.getInjectionPointsForProperty(normalizer.normalize(propertyTimeout));
        assertThat(timeoutPoints)
                .filteredOn(point -> point.getInjectionType() == InjectionType.METHOD_PARAMETER
                        && point.getTargetName().contains("setMaxTimeout"))
                .hasSize(1)
                .first()
                .satisfies(point -> {
                    assertThat(point.getBeanName()).isEqualTo(SAMPLE_BEAN_NAME);
                    assertThat(point.getPropertyExpression()).isEqualTo("${" + propertyTimeout + ":5000}");
                });
    }

    @Test
    void testDirectMethodInjection() {
        // when.
        String propertyMethodTimeout = "test.method.timeout";

        // then.
        List<InjectionPoint> timeoutPoints =
                subject.getInjectionPointsForProperty(normalizer.normalize(propertyMethodTimeout));
        assertThat(timeoutPoints)
                .filteredOn(point -> point.getInjectionType() == InjectionType.METHOD
                        && point.getTargetName().contains("calculateRandomTimeout"))
                .hasSize(1)
                .first()
                .satisfies(point -> {
                    assertThat(point.getBeanName()).isEqualTo(SAMPLE_BEAN_NAME);
                    assertThat(point.getPropertyExpression()).isEqualTo("${" + propertyMethodTimeout + "}");
                });
    }

    @Test
    void testMetaAnnotationMethodInjection() {
        // when.
        String propertyAppTimeout = "test.app.timeout";

        // then.
        List<InjectionPoint> customTimeoutPoints =
                subject.getInjectionPointsForProperty(normalizer.normalize(propertyAppTimeout));
        assertThat(customTimeoutPoints)
                .filteredOn(point -> point.getInjectionType() == InjectionType.METHOD
                        && point.getTargetName().contains("getDefaultTimeout"))
                .hasSize(1)
                .first()
                .satisfies(point -> {
                    assertThat(point.getBeanName()).isEqualTo(SAMPLE_BEAN_NAME);
                    assertThat(point.getPropertyExpression()).isEqualTo("${" + propertyAppTimeout + ":5000}");
                });
    }

    @Test
    void testEnvironmentGetPropertySpEL() {
        // when.
        List<InjectionPoint> points = subject.getInjectionPointsForProperty(normalizer.normalize("server.port"));

        // then.
        assertThat(points)
                .filteredOn(p -> p.getTargetName().equals("envPort"))
                .hasSize(1)
                .first()
                .satisfies(p -> {
                    assertThat(p.getInjectionType()).isEqualTo(InjectionType.FIELD);
                    assertThat(p.getPropertyExpression()).isEqualTo("#{environment.getProperty('server.port')}");
                });
    }

    @Test
    void testSystemPropertiesSpEL() {
        // when.
        List<InjectionPoint> points = subject.getInjectionPointsForProperty(normalizer.normalize("user.home"));

        // then.
        assertThat(points)
                .filteredOn(p -> p.getTargetName().equals("systemHome"))
                .hasSize(1)
                .first()
                .satisfies(p -> {
                    assertThat(p.getInjectionType()).isEqualTo(InjectionType.FIELD);
                    assertThat(p.getPropertyExpression()).isEqualTo("#{systemProperties['user.home']}");
                });
    }
}
