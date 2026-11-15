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
package com.axelixlabs.axelix.sbs.spring.core.conditions.profile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises real Spring registration with the startup agent, not a re-evaluation of profiles.
 *
 * @author Axelix Labs
 */
class ProfileDecisionAgentTest {

    @ParameterizedTest
    @ValueSource(strings = {"dev", "prod", "maintenance"})
    void recordsActualBeanMethodDecisionDuringBootStartup(String activeProfile) {
        // given.
        SpringApplicationBuilder application = new SpringApplicationBuilder(MethodConfiguration.class)
                .web(WebApplicationType.NONE)
                .profiles(activeProfile)
                .properties("spring.main.banner-mode=off");

        // when.
        try (ConfigurableApplicationContext context = application.run()) {
            List<ProfileDecision> observations = ProfileDecisionRecorder.snapshot(context.getBeanFactory());

            // then.
            boolean expectedMatch = activeProfile.equals("prod");
            assertThat(context.containsBean("profiledBean")).isEqualTo(expectedMatch);
            assertThat(observations).anySatisfy(decision -> {
                assertThat(decision.className()).isEqualTo(MethodConfiguration.class.getName());
                assertThat(decision.methodName()).isEqualTo("profiledBean");
                assertThat(decision.profileExpressions()).containsExactly("prod & !maintenance");
                assertThat(decision.activeProfiles()).containsExactly(activeProfile);
                assertThat(decision.defaultProfiles()).containsExactly("default");
                assertThat(decision.matched()).isEqualTo(expectedMatch);
            });
            assertThat(observations).noneMatch(decision -> decision.methodName().equals("unconditionalBean"));
        }
    }

    @Test
    void recordsRejectedConfigurationWithoutInventingDecisionsForItsBeanMethods() {
        // given.
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("dev");
            context.register(ImportingConfiguration.class);

            // when.
            context.refresh();
            List<ProfileDecision> observations = ProfileDecisionRecorder.snapshot(context.getBeanFactory());

            // then.
            assertThat(context.containsBean("nestedBean")).isFalse();
            assertThat(observations).anySatisfy(decision -> {
                assertThat(decision.className()).isEqualTo(ProductionConfiguration.class.getName());
                assertThat(decision.methodName()).isEmpty();
                assertThat(decision.matched()).isFalse();
            });
            assertThat(observations).noneMatch(decision -> decision.methodName().equals("nestedBean"));
        }
    }

    @Test
    void recordsComponentRejectedDuringScanning() {
        // given.
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("dev");
            ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);

            // when.
            scanner.scan("com.axelixlabs.axelix.profilefixtures");
            context.refresh();

            // then.
            assertThat(context.containsBean("productionComponent")).isFalse();
            assertThat(ProfileDecisionRecorder.snapshot(context.getBeanFactory()))
                    .anySatisfy(decision -> {
                        assertThat(decision.className())
                                .isEqualTo("com.axelixlabs.axelix.profilefixtures.ProductionComponent");
                        assertThat(decision.profileExpressions()).containsExactly("prod");
                        assertThat(decision.matched()).isFalse();
                    });
        }
    }

    @Test
    void recordsDefaultAndComposedProfileSemantics() {
        // given.
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles();
            context.getEnvironment().setDefaultProfiles("fallback");
            context.register(DefaultConfiguration.class);

            // when.
            context.refresh();

            // then.
            assertThat(context.containsBean("defaultBean")).isTrue();
            assertThat(ProfileDecisionRecorder.snapshot(context.getBeanFactory()))
                    .anySatisfy(decision -> {
                        assertThat(decision.methodName()).isEqualTo("defaultBean");
                        assertThat(decision.profileExpressions()).containsExactly("fallback", "prod");
                        assertThat(decision.activeProfiles()).isEmpty();
                        assertThat(decision.defaultProfiles()).containsExactly("fallback");
                        assertThat(decision.matched()).isTrue();
                    });
        }
    }

    @Test
    void doesNotInventProfileDecisionWhenAnEarlierConditionRejectsDeclaration() {
        // given.
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("dev");
            context.register(RejectedBeforeProfile.class);

            // when.
            context.refresh();

            // then.
            assertThat(context.getBeanNamesForType(RejectedBeforeProfile.class)).isEmpty();
            assertThat(ProfileDecisionRecorder.snapshot(context.getBeanFactory()))
                    .isEmpty();
        }
    }

    @Test
    void doesNotRecordAThrowingConditionAsARejectedDeclaration() {
        // given.
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            // when / then.
            assertThatThrownBy(() -> context.register(InvalidProfileConfiguration.class))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(ProfileDecisionRecorder.snapshot(context.getBeanFactory()))
                    .isEmpty();
        }
    }

    @Test
    void capturesProfilesAtEvaluationTime() {
        // given.
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("dev");
            context.getEnvironment().setDefaultProfiles("fallback");
            context.register(ProductionConfiguration.class);

            // when.
            context.getEnvironment().setActiveProfiles("prod");
            context.getEnvironment().setDefaultProfiles("other");

            // then.
            assertThat(ProfileDecisionRecorder.snapshot(context.getBeanFactory()))
                    .singleElement()
                    .satisfies(decision -> {
                        assertThat(decision.activeProfiles()).containsExactly("dev");
                        assertThat(decision.defaultProfiles()).containsExactly("fallback");
                        assertThat(decision.matched()).isFalse();
                    });
        }
    }

    @Test
    void keepsRegistriesSeparateAndSnapshotsStableAcrossRepeatedEvaluations() {
        // given.
        try (AnnotationConfigApplicationContext first = new AnnotationConfigApplicationContext();
                AnnotationConfigApplicationContext second = new AnnotationConfigApplicationContext()) {
            first.getEnvironment().setActiveProfiles("dev");
            second.getEnvironment().setActiveProfiles("prod");
            first.register(ProductionConfiguration.class);
            List<ProfileDecision> original = ProfileDecisionRecorder.snapshot(first.getBeanFactory());

            // when.
            new AnnotatedBeanDefinitionReader(first).register(ProductionConfiguration.class);
            second.register(ProductionConfiguration.class);

            // then.
            assertThat(original).hasSize(1).allMatch(decision -> !decision.matched());
            assertThat(ProfileDecisionRecorder.snapshot(first.getBeanFactory()))
                    .hasSize(2)
                    .allMatch(decision -> !decision.matched());
            assertThat(ProfileDecisionRecorder.snapshot(second.getBeanFactory()))
                    .hasSize(1)
                    .allMatch(ProfileDecision::matched);
            assertThatThrownBy(original::clear).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MethodConfiguration {
        @Bean
        @Profile("prod & !maintenance")
        String profiledBean() {
            return "production";
        }

        @Bean
        String unconditionalBean() {
            return "always";
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Import(ProductionConfiguration.class)
    static class ImportingConfiguration {}

    @Configuration(proxyBeanMethods = false)
    @Profile("prod")
    static class ProductionConfiguration {
        @Bean
        String nestedBean() {
            return "production";
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Profile({"fallback", "prod"})
    @interface DefaultOrProduction {}

    @Configuration(proxyBeanMethods = false)
    static class DefaultConfiguration {
        @Bean
        @DefaultOrProduction
        String defaultBean() {
            return "default";
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("prod & dev | qa")
    static class InvalidProfileConfiguration {}

    @Configuration(proxyBeanMethods = false)
    @Conditional(RejectFirst.class)
    @Profile("prod")
    static class RejectedBeforeProfile {}

    static class RejectFirst implements Condition, Ordered {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return false;
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }
}
