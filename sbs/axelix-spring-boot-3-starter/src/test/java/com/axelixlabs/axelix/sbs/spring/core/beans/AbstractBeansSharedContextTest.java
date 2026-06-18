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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

import javax.sql.DataSource;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.sbs.spring.core.Main;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.conditions.ConditionalBeanRefBuilder;
import com.axelixlabs.axelix.sbs.spring.core.conditions.DefaultConditionalBeanRefBuilder;

/**
 * Shared base test that defines a single Spring {@link org.springframework.context.ApplicationContext}
 * reused by the "beans" tests so they hit the Spring TestContext cache instead of each building its
 * own context.
 *
 * <p>{@link Main} is pinned via the {@code classes} attribute (rather than left to Spring Boot's
 * auto-detection) so all subclasses resolve an identical, deterministic configuration — this is
 * what makes the cached context shareable, and it also keeps auto-configuration (e.g. the embedded
 * {@code DataSource}) active.
 *
 * <p>The context runs with a {@link SpringBootTest.WebEnvironment#RANDOM_PORT} web server and pulls
 * in the actuator beans endpoints. <strong>All</strong> test configurations consumed by the "beans"
 * tests live here, in the parent, as nested classes — so the parent owns the entire shared
 * configuration and never has to reference its subclasses.
 *
 * @author Artemiy Degtyarev
 */
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"axelix.prop.test.name=axelix-beans"})
@Import({
    AbstractBeansSharedContextTest.DefaultBeanAnalyzerTestConfig.class,
    AbstractBeansSharedContextTest.BeanMethodDeclarations.class,
    AbstractBeansSharedContextTest.ComponentMethodDeclarations.class,
    AbstractBeansSharedContextTest.ConfigurationClassesDeclarations.class,
    AbstractBeansSharedContextTest.CurrentConfiguration.class,
    BeansEndpoint.class,
    AxelixBeansEndpoint.class,
    ConditionsReportEndpoint.class,
    JwtAuthTestConfiguration.class
})
abstract class AbstractBeansSharedContextTest {

    // --- Endpoint integration configuration (AxelixBeansEndpointTest) ---

    @TestConfiguration(value = "testCurrentConfiguration")
    @EnableConfigurationProperties(AxelixPropTest.class)
    static class CurrentConfiguration {

        static final String QUALIFIERS_PERSISTENCE_POST_PROCESSOR = "qualifiersPersistencePostProcessor";
        static final String BEAN_META_INFO_EXTRACTOR = "beanMetaInfoExtractor";
        static final String CUSTOM_SUPPLIER = "customSupplier";

        @Bean
        public ConditionalBeanRefBuilder conditionalBeanRefBuilder() {
            return new DefaultConditionalBeanRefBuilder();
        }

        @Bean
        public BeansFeedBuilder testBeansFeedBuilder(
                BeanMetaInfoExtractor beanMetaInfoExtractor,
                ConfigurableApplicationContext configurableApplicationContext) {
            return new DefaultBeansFeedBuilder(beanMetaInfoExtractor, configurableApplicationContext);
        }

        @Bean(BEAN_META_INFO_EXTRACTOR)
        public BeanMetaInfoExtractor beanMetaInfoExtractor(
                ConfigurableApplicationContext configurableApplicationContext,
                ConditionalBeanRefBuilder conditionalBeanRefBuilder) {
            return new DefaultBeanMetaInfoExtractor(configurableApplicationContext, conditionalBeanRefBuilder);
        }

        @Bean(QUALIFIERS_PERSISTENCE_POST_PROCESSOR)
        public static QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
            return new QualifiersPersistencePostProcessor();
        }

        @Bean(CUSTOM_SUPPLIER)
        public Supplier<String> customSupplier() {
            return () -> "value";
        }
    }

    @ConfigurationProperties(prefix = "axelix.prop.test")
    static class AxelixPropTest {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // --- Bean meta-info extraction fixtures (DefaultBeanMetaInfoExtractorTest) ---

    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier(DefaultBeanAnalyzerTestConfig.CUSTOM_DATABASE_QUALIFIER_BEAN)
    public @interface CustomDatabaseQualifier {}

    /**
     * Static nested configuration that declares the various bean shapes exercised by the bean
     * meta-info extraction tests.
     */
    @TestConfiguration
    @EnableJpaRepositories(
            basePackageClasses = DefaultBeanAnalyzerTestConfig.MyRepository.class,
            considerNestedRepositories = true)
    @EntityScan(basePackageClasses = DefaultBeanAnalyzerTestConfig.MyEntity.class)
    public static class DefaultBeanAnalyzerTestConfig {

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

        @Bean(LAZY_PRIMARY_BEAN_METHOD)
        @Lazy
        @Primary
        public String lazyPrimaryBean() {
            return LAZY_PRIMARY_BEAN_METHOD;
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

        @Entity
        @Table(name = "my_entity")
        public static class MyEntity {
            @Id
            @GeneratedValue(strategy = GenerationType.AUTO)
            private Long id;
        }

        @Repository(SPRING_DATA_REPOSITORY)
        public interface MyRepository extends JpaRepository<MyEntity, Long> {}

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
        @CustomDatabaseQualifier
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

    // --- Qualifier persistence fixtures (QualifiersPersistencePostProcessorTest) ---

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
