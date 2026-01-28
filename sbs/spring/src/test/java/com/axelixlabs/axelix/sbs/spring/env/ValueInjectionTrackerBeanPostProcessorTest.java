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
package com.axelixlabs.axelix.sbs.spring.env;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionPoint;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionType;

import static com.axelixlabs.axelix.sbs.spring.env.ValueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations;
import static com.axelixlabs.axelix.sbs.spring.env.ValueInjectionTrackerBeanPostProcessorTest.TestBeanWithSpEL;
import static com.axelixlabs.axelix.sbs.spring.env.ValueInjectionTrackerBeanPostProcessorTest.ValueInjectionTrackerBeanPostProcessorTestConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ValueInjectionTrackerBeanPostProcessor}
 *
 * @since 16.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest(
        classes = {
            ValueInjectionTrackerBeanPostProcessorTestConfig.class,
            TestBeanWithCustomAnnotations.class,
            TestBeanWithSpEL.class
        })
@TestPropertySource(
        properties = {
            "test.server.port=9090",
            "test.spring.application.name=TimeoutTestApp",
            "test.spring.profiles.active=production",
            "test.app.timeout=3000",
            "test.inner.timeout=1500",
            "test.inner.constructor.timeout=2500",
            "test.method.timeout=4200"
        })
class ValueInjectionTrackerBeanPostProcessorTest {

    @TestConfiguration
    static class ValueInjectionTrackerBeanPostProcessorTestConfig {

        @Bean
        public static ValueInjectionTrackerBeanPostProcessor valueInjectionTrackerBeanPostProcessor() {
            return new ValueInjectionTrackerBeanPostProcessor(new DefaultPropertyNameNormalizer());
        }
    }

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
            assertThat(point.beanName())
                    .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
            assertThat(point.injectionType()).isEqualTo(InjectionType.FIELD);
            assertThat(point.targetName()).isEqualTo("serverPort");
            assertThat(point.propertyExpression()).isEqualTo("${" + propertyServerPort + ":8080}");
        });
    }

    @Test
    void testMetaAnnotationValueInjectionOnField() {
        // given.
        String propertyTimeout = "test.app.timeout";

        // when.
        List<InjectionPoint> points = subject.getInjectionPointsForProperty(normalizer.normalize(propertyTimeout));
        InjectionPoint injectionPoint = points.stream()
                .filter(point -> point.injectionType().equals(InjectionType.FIELD)
                        && point.targetName().equals("timeout"))
                .findAny()
                .orElseThrow();

        // then.
        assertThat(injectionPoint.beanName())
                .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
        assertThat(injectionPoint.propertyExpression()).isEqualTo("${" + propertyTimeout + ":5000}");
    }

    @Test
    void testDirectConstructorParameterInjection() {
        // when
        String propertyApplicationName = "test.spring.application.name";

        // then.
        List<InjectionPoint> appNamePoints =
                subject.getInjectionPointsForProperty(normalizer.normalize(propertyApplicationName));
        assertThat(appNamePoints).hasSize(1).first().satisfies(point -> {
            assertThat(point.beanName())
                    .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
            assertThat(point.injectionType()).isEqualTo(InjectionType.CONSTRUCTOR_PARAMETER);
            assertThat(point.targetName()).isEqualTo("appName");
            assertThat(point.propertyExpression()).isEqualTo("${" + propertyApplicationName + ":TestApp}");
        });
    }

    @Test
    void testMetaAnnotationValueOnConstructorParameterInjection() {
        // when.
        List<InjectionPoint> timeoutPoints =
                subject.getInjectionPointsForProperty(normalizer.normalize("test.app.timeout"));

        // then.
        assertThat(timeoutPoints)
                .filteredOn(point -> point.injectionType() == InjectionType.CONSTRUCTOR_PARAMETER
                        && point.targetName().equals("connectionTimeout"))
                .hasSize(1)
                .first()
                .satisfies(point -> {
                    assertThat(point.beanName())
                            .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
                    assertThat(point.propertyExpression()).isEqualTo("${test.app.timeout:5000}");
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
            assertThat(point.beanName())
                    .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
            assertThat(point.injectionType()).isEqualTo(InjectionType.METHOD_PARAMETER);
            assertThat(point.targetName()).contains("setProfile");
            assertThat(point.propertyExpression()).isEqualTo("${" + propertyProfile + "}");
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
                .filteredOn(point -> point.injectionType() == InjectionType.METHOD_PARAMETER
                        && point.targetName().contains("setMaxTimeout"))
                .hasSize(1)
                .first()
                .satisfies(point -> {
                    assertThat(point.beanName())
                            .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
                    assertThat(point.propertyExpression()).isEqualTo("${" + propertyTimeout + ":5000}");
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
                .filteredOn(point -> point.injectionType() == InjectionType.METHOD
                        && point.targetName().contains("calculateRandomTimeout"))
                .hasSize(1)
                .first()
                .satisfies(point -> {
                    assertThat(point.beanName())
                            .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
                    assertThat(point.propertyExpression()).isEqualTo("${" + propertyMethodTimeout + "}");
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
                .filteredOn(point -> point.injectionType() == InjectionType.METHOD
                        && point.targetName().contains("getDefaultTimeout"))
                .hasSize(1)
                .first()
                .satisfies(point -> {
                    assertThat(point.beanName())
                            .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
                    assertThat(point.propertyExpression()).isEqualTo("${" + propertyAppTimeout + ":5000}");
                });
    }

    @Test
    void testEnvironmentGetPropertySpEL() {
        // when.
        List<InjectionPoint> points = subject.getInjectionPointsForProperty(normalizer.normalize("server.port"));

        // then.
        assertThat(points)
                .filteredOn(p -> p.targetName().equals("envPort"))
                .hasSize(1)
                .first()
                .satisfies(p -> {
                    assertThat(p.injectionType()).isEqualTo(InjectionType.FIELD);
                    assertThat(p.propertyExpression()).isEqualTo("#{environment.getProperty('server.port')}");
                });
    }

    @Test
    void testSystemPropertiesSpEL() {
        // when.
        List<InjectionPoint> points = subject.getInjectionPointsForProperty(normalizer.normalize("user.home"));

        // then.
        assertThat(points)
                .filteredOn(p -> p.targetName().equals("systemHome"))
                .hasSize(1)
                .first()
                .satisfies(p -> {
                    assertThat(p.injectionType()).isEqualTo(InjectionType.FIELD);
                    assertThat(p.propertyExpression()).isEqualTo("#{systemProperties['user.home']}");
                });
    }

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Value("${test.app.timeout:5000}")
    public @interface TimeoutValue {}

    @Component
    public static class TestBeanWithSpEL {

        @Value("#{environment.getProperty('server.port')}")
        private String envPort;

        @Value("#{systemProperties['user.home']}")
        private String systemHome;

        @Value("#{environment.getProperty('app.timeout')}")
        public Integer getTimeout() {
            return 5000;
        }
    }

    @Component
    public static class TestBeanWithCustomAnnotations {

        @Value("${test.server.port:8080}")
        private String serverPort;

        @TimeoutValue
        private Integer timeout;

        public TestBeanWithCustomAnnotations(
                @Value("${test.spring.application.name:TestApp}") String appName,
                @TimeoutValue String connectionTimeout) {}

        private String profile;
        private Integer maxTimeout;

        @Autowired
        public void setProfile(@Value("${test.spring.profiles.active}") String profile) {
            this.profile = profile;
        }

        @Autowired
        public void setMaxTimeout(@TimeoutValue Integer timeout) {
            this.maxTimeout = timeout * 2;
        }

        @Value("${test.method.timeout}")
        public void calculateRandomTimeout() {}

        @TimeoutValue
        public void getDefaultTimeout() {}

        public String getServerPort() {
            return serverPort;
        }
    }
}
