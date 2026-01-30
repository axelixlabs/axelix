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
package com.axelixlabs.axelix.sbs.spring.beans;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Integration test for {@link QualifiersPersistencePostProcessor}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
@Import({
    QualifiersPersistencePostProcessorTest.BeanMethodDeclarations.class,
    QualifiersPersistencePostProcessorTest.ComponentMethodDeclarations.class,
    QualifiersPersistencePostProcessorTest.ConfigurationClassesDeclarations.class,
    QualifiersPersistencePostProcessor.class
})
class QualifiersPersistencePostProcessorTest {

    @Test
    void shouldDetectAnnotationQualifiers() {
        DefaultQualifiersRegistry registry = DefaultQualifiersRegistry.INSTANCE;

        SoftAssertions.assertSoftly(sa -> {
            sa.assertThat(registry.getQualifiers("noQualifiersBeanName")).isEmpty();
            sa.assertThat(registry.getQualifiers("builtInTypeQualifierBeanName"))
                    .containsOnly("builtInTypeQualifier");
            sa.assertThat(registry.getQualifiers("customTypeQualifierBeanName")).containsOnly("customQualifier");
            sa.assertThat(registry.getQualifiers("mixedTypeQualifierBeanName"))
                    .containsOnly("builtInTypeQualifier", "customQualifier");
        });

        SoftAssertions.assertSoftly(sa -> {
            sa.assertThat(registry.getQualifiers("beanMethodNoQualifiersBeanName"))
                    .isEmpty();
            sa.assertThat(registry.getQualifiers("beanMethodBuiltInTypeQualifierBeanName"))
                    .containsOnly("builtInTypeQualifier");
            sa.assertThat(registry.getQualifiers("beanMethodCustomTypeQualifierBeanName"))
                    .containsOnly("customQualifier");
            sa.assertThat(registry.getQualifiers("beanMethodMixedTypeQualifierBeanName"))
                    .containsOnly("builtInTypeQualifier", "customQualifier");
        });

        SoftAssertions.assertSoftly(sa -> {
            sa.assertThat(registry.getQualifiers("noQualifiersConfigBeanName")).isEmpty();
            sa.assertThat(registry.getQualifiers("builtInTypeQualifierConfigBeanName"))
                    .containsOnly("builtInTypeQualifier");
            sa.assertThat(registry.getQualifiers("customTypeQualifierConfigBeanName"))
                    .containsOnly("customQualifier");
            sa.assertThat(registry.getQualifiers("mixedTypeQualifierConfigBeanName"))
                    .containsOnly("builtInTypeQualifier", "customQualifier");
        });
    }

    @Documented
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier("customQualifier")
    @interface CustomQualifier {}

    @TestConfiguration
    static class ComponentMethodDeclarations {

        @Component("noQualifiersBeanName")
        static class NoQualifiers {}

        @Service("builtInTypeQualifierBeanName")
        @Qualifier("builtInTypeQualifier")
        static class BuiltInTypeQualifier {}

        @Service("customTypeQualifierBeanName")
        @CustomQualifier
        static class CustomTypeQualifier {}

        @Service("mixedTypeQualifierBeanName")
        @CustomQualifier
        @Qualifier("builtInTypeQualifier")
        static class MixedTypeQualifier {}
    }

    @TestConfiguration
    static class BeanMethodDeclarations {

        static class BeanMethodNoQualifiers {}

        static class BeanMethodBuiltInTypeQualifier {}

        static class BeanMethodCustomTypeQualifier {}

        static class BeanMethodMixedTypeQualifier {}

        @Bean("beanMethodNoQualifiersBeanName")
        public BeanMethodNoQualifiers beanMethodNoQualifiers() {
            return new BeanMethodNoQualifiers();
        }

        @Bean("beanMethodBuiltInTypeQualifierBeanName")
        @Qualifier("builtInTypeQualifier")
        public BeanMethodBuiltInTypeQualifier builtInTypeQualifier() {
            return new BeanMethodBuiltInTypeQualifier();
        }

        @Bean("beanMethodCustomTypeQualifierBeanName")
        @CustomQualifier
        public BeanMethodCustomTypeQualifier customTypeQualifier() {
            return new BeanMethodCustomTypeQualifier();
        }

        @Bean("beanMethodMixedTypeQualifierBeanName")
        @CustomQualifier
        @Qualifier("builtInTypeQualifier")
        public BeanMethodMixedTypeQualifier mixedTypeQualifier() {
            return new BeanMethodMixedTypeQualifier();
        }
    }

    @Configuration
    static class ConfigurationClassesDeclarations {

        @Configuration("noQualifiersConfigBeanName")
        static class NoQualifiers {}

        @Configuration("builtInTypeQualifierConfigBeanName")
        @Qualifier("builtInTypeQualifier")
        static class BuiltInTypeQualifier {}

        @Configuration("customTypeQualifierConfigBeanName")
        @CustomQualifier
        static class CustomTypeQualifier {}

        @Configuration("mixedTypeQualifierConfigBeanName")
        @CustomQualifier
        @Qualifier("builtInTypeQualifier")
        static class MixedTypeQualifier {}
    }
}
