/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.env;

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
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed.InjectionPoint;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.InjectionType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ValueInjectionTrackerBeanPostProcessor}
 *
 * @since 16.12.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(
        classes = {
            ValueInjectionTrackerBeanPostProcessorTest.ValueInjectionTrackerBeanPostProcessorTestConfig.class,
            ValueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations.class,
            ValueInjectionTrackerBeanPostProcessorTest.TestBeanWithSpEL.class
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
@Import(ValueInjectionTrackerBeanPostProcessorTest.ValueInjectionTrackerBeanPostProcessorTestConfig.class)
class ValueInjectionTrackerBeanPostProcessorTest {

    @TestConfiguration
    static class ValueInjectionTrackerBeanPostProcessorTestConfig {

        @Bean
        public static ValueInjectionTrackerBeanPostProcessor valueInjectionTrackerBeanPostProcessor() {
            return new ValueInjectionTrackerBeanPostProcessor(new DefaultPropertyNameNormalizer());
        }
    }

    @Autowired
    private ValueInjectionTrackerBeanPostProcessor tracker;

    @Test
    void testValueInjectionOnField() {
        String propertyServerPort = "test.server.port";

        List<InjectionPoint> points = tracker.getInjectionPointsForProperty(propertyServerPort);

        assertThat(points).hasSize(1).first().satisfies(point -> {
            assertThat(point.beanName())
                    .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
            assertThat(point.injectionType()).isEqualTo(InjectionType.FIELD);
            assertThat(point.targetName()).isEqualTo("serverPort");
            assertThat(point.propertyExpression()).isEqualTo("${" + propertyServerPort + ":8080}");
        });

        String propertyTimeout = "test.app.timeout";

        points = tracker.getInjectionPointsForProperty(propertyTimeout);
        InjectionPoint injectionPoint = points.stream()
                .filter(point -> point.injectionType().equals(InjectionType.FIELD)
                        && point.targetName().equals("timeout"))
                .findAny()
                .orElseThrow();

        assertThat(injectionPoint.beanName())
                .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
        assertThat(injectionPoint.propertyExpression()).isEqualTo("${" + propertyTimeout + ":5000}");
    }

    @Test
    void testConstructorParameterInjections() {
        String propertyApplicationName = "test.spring.application.name";

        List<InjectionPoint> appNamePoints = tracker.getInjectionPointsForProperty(propertyApplicationName);
        assertThat(appNamePoints).hasSize(1).first().satisfies(point -> {
            assertThat(point.beanName())
                    .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
            assertThat(point.injectionType()).isEqualTo(InjectionType.CONSTRUCTOR_PARAMETER);
            assertThat(point.targetName()).isEqualTo("appName");
            assertThat(point.propertyExpression()).isEqualTo("${" + propertyApplicationName + ":TestApp}");
        });

        List<InjectionPoint> timeoutPoints = tracker.getInjectionPointsForProperty("test.app.timeout");
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
    void testMethodParameterInjections() {
        String propertyProfile = "test.spring.profiles.active";

        List<InjectionPoint> profilePoints = tracker.getInjectionPointsForProperty(propertyProfile);
        assertThat(profilePoints).hasSize(1).first().satisfies(point -> {
            assertThat(point.beanName())
                    .isEqualTo("valueInjectionTrackerBeanPostProcessorTest.TestBeanWithCustomAnnotations");
            assertThat(point.injectionType()).isEqualTo(InjectionType.METHOD_PARAMETER);
            assertThat(point.targetName()).contains("setProfile");
            assertThat(point.propertyExpression()).isEqualTo("${" + propertyProfile + "}");
        });

        String propertyTimeout = "test.app.timeout";

        List<InjectionPoint> timeoutPoints = tracker.getInjectionPointsForProperty(propertyTimeout);
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
    void testMethodInjections() {
        String propertyMethodTimeout = "test.method.timeout";

        List<InjectionPoint> timeoutPoints = tracker.getInjectionPointsForProperty(propertyMethodTimeout);
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

        String propertyAppTimeout = "test.app.timeout";

        List<InjectionPoint> customTimeoutPoints = tracker.getInjectionPointsForProperty(propertyAppTimeout);
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
    void testInnerClassInjections() {
        String propertyInnerTimeout = "test.inner.timeout";

        List<InjectionPoint> innerTimeoutPoints = tracker.getInjectionPointsForProperty(propertyInnerTimeout);
        assertThat(innerTimeoutPoints)
                .filteredOn(point -> point.injectionType() == InjectionType.FIELD)
                .isNotEmpty();

        String propertyInnerConstructorTimeout = "test.inner.constructor.timeout";

        List<InjectionPoint> innerConstructorPoints =
                tracker.getInjectionPointsForProperty(propertyInnerConstructorTimeout);
        assertThat(innerConstructorPoints)
                .filteredOn(point -> point.injectionType() == InjectionType.CONSTRUCTOR_PARAMETER)
                .isNotEmpty();

        String propertyAppTimeout = "test.app.timeout";

        List<InjectionPoint> timeoutPoints = tracker.getInjectionPointsForProperty(propertyAppTimeout);
        assertThat(timeoutPoints)
                .filteredOn(point -> point.injectionType() == InjectionType.FIELD
                        && point.beanName().contains("InnerTimeoutBean")
                        && point.targetName().equals("defaultInnerTimeout"))
                .hasSize(1)
                .first()
                .satisfies(point -> {
                    assertThat(point.beanName()).contains("InnerTimeoutBean");
                    assertThat(point.injectionType()).isEqualTo(InjectionType.FIELD);
                    assertThat(point.targetName()).isEqualTo("defaultInnerTimeout");
                    assertThat(point.propertyExpression()).isEqualTo("${" + propertyAppTimeout + ":5000}");
                });
    }

    @Test
    void testEnvironmentGetPropertySpEL() {
        List<InjectionPoint> points = tracker.getInjectionPointsForProperty("server.port");

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
        List<InjectionPoint> points = tracker.getInjectionPointsForProperty("user.home");

        assertThat(points)
                .filteredOn(p -> p.targetName().equals("systemHome"))
                .hasSize(1)
                .first()
                .satisfies(p -> {
                    assertThat(p.injectionType()).isEqualTo(InjectionType.FIELD);
                    assertThat(p.propertyExpression()).isEqualTo("#{systemProperties['user.home']}");
                });
    }

    @Test
    void testMethodSpEL() {
        List<InjectionPoint> points = tracker.getInjectionPointsForProperty("app.timeout");

        assertThat(points)
                .filteredOn(p -> p.targetName().contains("getTimeout"))
                .hasSize(1)
                .first()
                .satisfies(p -> {
                    assertThat(p.injectionType()).isEqualTo(InjectionType.METHOD);
                    assertThat(p.propertyExpression()).isEqualTo("#{environment.getProperty('app.timeout')}");
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

        private final String appName;

        private final Integer connectionTimeout;

        public TestBeanWithCustomAnnotations(
                @Value("${test.spring.application.name:TestApp}") String appName,
                @TimeoutValue Integer connectionTimeout) {
            this.appName = appName;
            this.connectionTimeout = connectionTimeout;
        }

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

        @Component
        public static class InnerTimeoutBean {

            @Value("${test.inner.timeout:1000}")
            private Integer innerTimeout;

            @TimeoutValue
            private Integer defaultInnerTimeout;

            public InnerTimeoutBean(@Value("${test.inner.constructor.timeout:2000}") Integer constructorTimeout) {}
        }

        public String getServerPort() {
            return serverPort;
        }
    }
}
