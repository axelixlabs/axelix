package com.nucleonforge.axile.spring.beans;

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
