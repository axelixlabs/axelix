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
package com.axelixlabs.axelix.sbs.spring.core.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bean declarations that drive {@link DefaultBeanMetaInfoExtractorTest}. Imported into the shared
 * endpoint test context so each fixture bean is present when the test asks
 * {@link BeanMetaInfoExtractor} to extract its meta-info.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@TestConfiguration
public class DefaultBeanMetaInfoExtractorTestFixtures {

    public static final String PRIMARY_COMPONENT = "primaryComponent";

    public static final String LAZY_COMPONENT = "lazyComponentAnnotation";

    public static final String REGULAR_COMPONENT = "fromServiceAnnotation";

    public static final String QUALIFIED_COMPONENT = "qualifiedService";

    public static final String LAZY_PRIMARY_BEAN_METHOD = "lazyPrimaryBeanMethod";

    public static final String QUALIFIED_BEAN_METHOD = "qualifiedBeanMethod";

    public static final String CONFIGURATION_BEAN = "configurationBean";

    public static final String TRANSACTIONAL_BEAN = "transactionalBean";

    public static final String SPRING_DATA_REPOSITORY = MyRepository.BEAN_NAME;

    public static final String CUSTOM_DATABASE_QUALIFIER_BEAN = "CustomDatabaseQualifierBean";

    public static final String ANONYMOUS_BEAN = "anonymousBean";

    public static final String STATIC_BFPP_BEAN = "staticBFPPBean";

    public static final String BEAN_DEFINITION_REGISTRY_BEAN = "BEAN_DEFINITION_REGISTRY_BEAN";

    public static final String SYNTHETIC_BEAN_DEFINITION = "SYNTHETIC_BEAN_DEFINITION";

    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier(CUSTOM_DATABASE_QUALIFIER_BEAN)
    public @interface CustomDatabaseQualifier {}

    @Service(REGULAR_COMPONENT)
    static class FromServiceAnnotation {}

    @Component(LAZY_COMPONENT)
    @Lazy
    static class LazyComponentAnnotation {}

    @Component(PRIMARY_COMPONENT)
    @Primary
    static class PrimaryComponent {}

    @Service(QUALIFIED_COMPONENT)
    @Qualifier(QUALIFIED_COMPONENT)
    static class QualifiedService {}

    @Configuration(CONFIGURATION_BEAN)
    public static class ConfigurationBean {}

    @Service(TRANSACTIONAL_BEAN)
    public static class TransactionalClass {

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Transactional
        public void execute() {
            // code
        }
    }

    @Service(CUSTOM_DATABASE_QUALIFIER_BEAN)
    @CustomDatabaseQualifier
    static class CustomDatabaseQualifierBean {}

    @Bean(LAZY_PRIMARY_BEAN_METHOD)
    @Lazy
    @Primary
    public String lazyPrimaryBean() {
        return LAZY_PRIMARY_BEAN_METHOD;
    }

    @Bean
    @Qualifier(QUALIFIED_BEAN_METHOD)
    public String qualifiedBeanMethod() {
        return QUALIFIED_BEAN_METHOD;
    }

    // IMPORTANT! Intentionally using explicit anonymous class `new Runnable()`
    // instead of lambda to ensure Class.isAnonymousClass() returns true.
    @Bean(ANONYMOUS_BEAN)
    public Runnable anonymousBean() {
        return new Runnable() {
            @Override
            public void run() {}
        };
    }

    @Bean(STATIC_BFPP_BEAN)
    public static BeanFactoryPostProcessor staticBFPPBean() {
        return beanFactory -> {};
    }

    @Bean(BEAN_DEFINITION_REGISTRY_BEAN)
    public static BeanDefinitionRegistryPostProcessor beanDefinitionRegistryPostProcessor() {

        return new BeanDefinitionRegistryPostProcessor() {

            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                RootBeanDefinition beanDefinition = new RootBeanDefinition();
                beanDefinition.setSynthetic(true);
                beanDefinition.setBeanClass(Object.class);
                registry.registerBeanDefinition(SYNTHETIC_BEAN_DEFINITION, beanDefinition);
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}
        };
    }
}
