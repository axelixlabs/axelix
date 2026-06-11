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

import com.axelixlabs.axelix.sbs.spring.core.conditions.ConditionalBeanRefBuilder;
import com.axelixlabs.axelix.sbs.spring.core.conditions.DefaultConditionalBeanRefBuilder;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.sql.DataSource;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Base class for the beans-related integration tests.
 *
 * <p>Every beans test extends this class and declares no context-affecting annotations of its own
 * (no {@code @SpringBootTest}, {@code @TestPropertySource}, {@code @Import} or nested
 * {@code @TestConfiguration} classes). As a result, all the beans tests produce an identical
 * merged context configuration and therefore share a single cached Spring application context,
 * which is only started once for the whole test run.
 *
 * <p>{@link AxelixBeansEndpointTest} is intentionally not part of this hierarchy.
 *
 * <p>The annotations below are the union of the configuration previously declared by the
 * individual beans tests.
 */
@SpringBootTest
@Import({
    AbstractBeansIntegrationTest.BeansTestConfiguration.class
})
public abstract class AbstractBeansIntegrationTest {
    @TestConfiguration
    @EnableJpaRepositories(
        basePackageClasses = BeansTestConfiguration.MyRepository.class,
        considerNestedRepositories = true)
    @EntityScan(basePackageClasses = BeansTestConfiguration.MyEntity.class)
    static class BeansTestConfiguration {
        static final String PRIMARY_COMPONENT = "primaryComponent";

        static final String LAZY_COMPONENT = "lazyComponentAnnotation";

        static final String REGULAR_COMPONENT = "fromServiceAnnotation";

        static final String QUALIFIED_COMPONENT = "qualifiedService";

        static final String LAZY_PRIMARY_BEAN_METHOD = "lazyPrimaryBeanMethod";

        static final String QUALIFIED_BEAN_METHOD = "qualifiedBeanMethod";

        static final String CONFIGURATION_BEAN = "configurationBean";

        static final String TRANSACTIONAL_BEAN = "transactionalBean";

        static final String SPRING_DATA_REPOSITORY = "MyRepository";

        static final String CUSTOM_DATABASE_QUALIFIER_BEAN = "CustomDatabaseQualifierBean";

        static final String ANONYMOUS_BEAN = "anonymousBean";

        static final String STATIC_BFPP_BEAN = "staticBFPPBean";

        static final String BEAN_DEFINITION_REGISTRY_BEAN = "BEAN_DEFINITION_REGISTRY_BEAN";

        static final String SYNTHETIC_BEAN_DEFINITION = "SYNTHETIC_BEAN_DEFINITION";

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

        static class BeanMethodNoQualifiers {}

        static class BeanMethodBuiltInTypeQualifier {}

        static class BeanMethodCustomTypeQualifier {}

        static class BeanMethodMixedTypeQualifier {}

        @Component("noQualifiersBeanName")
        static class ComponentNoQualifiers {}

        @Service("builtInTypeQualifierBeanName")
        @Qualifier("builtInTypeQualifier")
        static class ComponentBuiltInTypeQualifier {}

        @Service("customTypeQualifierBeanName")
        @CustomQualifier
        static class ComponentCustomTypeQualifier {}

        @Service("mixedTypeQualifierBeanName")
        @CustomQualifier
        @Qualifier("builtInTypeQualifier")
        static class ComponentMixedTypeQualifier {}

        @Configuration("noQualifiersConfigBeanName")
        static class ConfigurationNoQualifiers {}

        @Configuration("builtInTypeQualifierConfigBeanName")
        @Qualifier("builtInTypeQualifier")
        static class ConfigurationBuiltInTypeQualifier {}

        @Configuration("customTypeQualifierConfigBeanName")
        @CustomQualifier
        static class ConfigurationCustomTypeQualifier {}

        @Configuration("mixedTypeQualifierConfigBeanName")
        @CustomQualifier
        @Qualifier("builtInTypeQualifier")
        static class ConfigurationMixedTypeQualifier {}

        @Bean("beanMethodNoQualifiersBeanName")
        public BeanMethodNoQualifiers beanMethodNoQualifiers() {
            return new BeanMethodNoQualifiers();
        }

        @Bean("beanMethodBuiltInTypeQualifierBeanName")
        @Qualifier("builtInTypeQualifier")
        public BeanMethodBuiltInTypeQualifier builtInTypeQualifier() {
            return new BeanMethodBuiltInTypeQualifier();
        }

        @Documented
        @Target({ElementType.TYPE, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier("customQualifier")
        @interface CustomQualifier {}

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

        @Bean(LAZY_PRIMARY_BEAN_METHOD)
        @Lazy
        @Primary
        public String lazyPrimaryBean() {
            return LAZY_PRIMARY_BEAN_METHOD;
        }

        @Bean
        public static QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
            return new QualifiersPersistencePostProcessor();
        }

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
            emf.setDataSource(dataSource);
            emf.setPackagesToScan(MyEntity.class.getPackageName());

            JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            emf.setJpaVendorAdapter(vendorAdapter);

            emf.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "create-drop");
            return emf;
        }

        @Bean
        public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }

        @Bean
        public ConditionalBeanRefBuilder conditionalBeanRefBuilder() {
            return new DefaultConditionalBeanRefBuilder();
        }

        @Bean
        public BeanMetaInfoExtractor beanMetaInfoExtractor(
            ConfigurableApplicationContext configurableApplicationContext,
            ConditionalBeanRefBuilder conditionalBeanRefBuilder) {
            return new DefaultBeanMetaInfoExtractor(configurableApplicationContext, conditionalBeanRefBuilder);
        }

        @Bean
        @Qualifier(QUALIFIED_BEAN_METHOD)
        public String qualifiedBeanMethod() {
            return QUALIFIED_BEAN_METHOD;
        }

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
        @DefaultBeanMetaInfoExtractorTest.CustomDatabaseQualifier
        static class CustomDatabaseQualifierBean {}

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
                public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
                    RootBeanDefinition beanDefinition = new RootBeanDefinition();
                    beanDefinition.setSynthetic(true);
                    beanDefinition.setBeanClass(Object.class);
                    registry.registerBeanDefinition(SYNTHETIC_BEAN_DEFINITION, beanDefinition);
                }

                @Override
                public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {}
            };
        }

        @Entity
        @Table(name = "my_entity")
        public static class MyEntity {
            @Id
            @GeneratedValue(strategy = GenerationType.AUTO)
            private Long id;
        }

        @Repository(SPRING_DATA_REPOSITORY)
        public interface MyRepository extends JpaRepository<MyEntity, Long> {}
    }
}
