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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.junit.jupiter.api.Test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
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

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.common.api.BeansFeed.ComponentVariant;
import com.axelixlabs.axelix.sbs.spring.conditions.ConditionalBeanRefBuilder;
import com.axelixlabs.axelix.sbs.spring.conditions.DefaultConditionalBeanRefBuilder;

import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.ANONYMOUS_BEAN;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.CONFIGURATION_BEAN;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.CUSTOM_DATABASE_QUALIFIER_BEAN;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.LAZY_COMPONENT;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.LAZY_PRIMARY_BEAN_METHOD;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.PRIMARY_COMPONENT;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.QUALIFIED_BEAN_METHOD;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.QUALIFIED_COMPONENT;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.REGULAR_COMPONENT;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.SPRING_DATA_REPOSITORY;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.STATIC_BFPP_BEAN;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.SYNTHETIC_BEAN_DEFINITION;
import static com.axelixlabs.axelix.sbs.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.TRANSACTIONAL_BEAN;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultBeanMetaInfoExtractor}.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.class)
class DefaultBeanMetaInfoExtractorTest {

    @Autowired
    private BeanMetaInfoExtractor metaInfoExtractor;

    @Autowired
    private ConfigurableListableBeanFactory testBeanFactory;

    @Test
    void shouldExtractForSimpleServiceBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(REGULAR_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.qualifiers()).isEmpty();
            assertThat(it.beanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.autoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractForAutoConfigurationBeanClass() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(CacheAutoConfiguration.class.getName(), testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.qualifiers()).isEmpty();
            assertThat(it.autoConfigurationRef()).isEqualTo("CacheAutoConfiguration");
            assertThat(it.beanSource()).isInstanceOf(ComponentVariant.class);
        });
    }

    @Test
    void shouldExtractForAutoConfigurationBeanMethod() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract("cacheManagerCustomizers", testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.qualifiers()).isEmpty();
            assertThat(it.autoConfigurationRef()).isEqualTo("CacheAutoConfiguration#cacheManagerCustomizers");
            assertThat(it.beanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat((BeansFeed.BeanMethod) it.beanSource()).satisfies(beanMethod -> {
                assertThat(beanMethod.enclosingClassFullName()).isEqualTo(CacheAutoConfiguration.class.getName());
                assertThat(beanMethod.methodName()).isEqualTo("cacheManagerCustomizers");
            });
        });
    }

    @Test
    void shouldExtractLazyServiceBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(LAZY_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isTrue();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.qualifiers()).isEmpty();
            assertThat(it.beanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.autoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractPrimaryComponentBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(PRIMARY_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isTrue();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.qualifiers()).isEmpty();
            assertThat(it.beanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.autoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractQualifiedServiceBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(QUALIFIED_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.beanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.qualifiers()).contains(QUALIFIED_COMPONENT);
            assertThat(it.autoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractLazyPrimaryBeanMethod() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(LAZY_PRIMARY_BEAN_METHOD, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isTrue();
            assertThat(it.isPrimary()).isTrue();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.qualifiers()).isEmpty();
            assertThat(it.beanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.autoConfigurationRef()).isNull();
            assertThat((BeansFeed.BeanMethod) it.beanSource()).satisfies(bs -> {
                assertThat(bs.enclosingClassName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getSimpleName());
                assertThat(bs.enclosingClassFullName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getName());
                assertThat(bs.methodName()).isEqualTo("lazyPrimaryBean");
            });
        });
    }

    @Test
    void shouldExtractQualifiedBeanMethod() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(QUALIFIED_BEAN_METHOD, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.qualifiers()).contains(QUALIFIED_BEAN_METHOD);
            assertThat(it.beanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.autoConfigurationRef()).isNull();
            assertThat((BeansFeed.BeanMethod) it.beanSource()).satisfies(bs -> {
                assertThat(bs.enclosingClassName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getSimpleName());
                assertThat(bs.enclosingClassFullName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getName());
                assertThat(bs.methodName()).isEqualTo(QUALIFIED_BEAN_METHOD);
            });
        });
    }

    @Test
    void shouldExtractSpringDataRepositoryBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(SPRING_DATA_REPOSITORY, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.JDK_PROXY);
            assertThat(it.qualifiers()).isEmpty();
            assertThat(it.beanSource()).isInstanceOf(BeansFeed.FactoryBean.class);
            assertThat(it.autoConfigurationRef()).isNull();
            assertThat((BeansFeed.FactoryBean) it.beanSource()).satisfies(bs -> {
                assertThat(bs.factoryBeanName()).isEqualTo(JpaRepositoryFactoryBean.class.getName());
            });
        });
    }

    @Test
    void shouldExtractCustomQualifierAnnotations() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(CUSTOM_DATABASE_QUALIFIER_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.qualifiers()).contains(CUSTOM_DATABASE_QUALIFIER_BEAN);
            assertThat(it.beanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.autoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractConfigurationBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(CONFIGURATION_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.CGLIB);
            assertThat(it.beanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.autoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractTransactionalBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(TRANSACTIONAL_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.CGLIB);
            assertThat(it.beanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.autoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractBeanFromAnonymousClass() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(ANONYMOUS_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.beanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.autoConfigurationRef()).isNull();

            BeansFeed.BeanMethod source = (BeansFeed.BeanMethod) it.beanSource();
            assertThat(source.enclosingClassName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getSimpleName());
            assertThat(source.enclosingClassFullName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getName());
            assertThat(source.methodName()).isEqualTo(ANONYMOUS_BEAN);
        });
    }

    @Test
    void shouldExtractInfoForStaticBeanFactoryPostProcessorBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(STATIC_BFPP_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.beanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.autoConfigurationRef()).isNull();

            BeansFeed.BeanMethod source = (BeansFeed.BeanMethod) it.beanSource();
            assertThat(source.enclosingClassName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getSimpleName());
            assertThat(source.enclosingClassFullName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getName());
            assertThat(source.methodName()).isEqualTo(STATIC_BFPP_BEAN);
        });
    }

    @Test
    void shouldExtractInfoForSyntheticallyGeneratedBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(SYNTHETIC_BEAN_DEFINITION, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.beanSource()).isInstanceOf(BeansFeed.SyntheticBean.class);
            assertThat(it.autoConfigurationRef()).isNull();
        });
    }

    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier(CUSTOM_DATABASE_QUALIFIER_BEAN)
    public @interface CustomDatabaseQualifier {}

    /**
     * Static nested configuration class for {@link DefaultBeanMetaInfoExtractorTest}.
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
        public ConditionalBeanRefBuilder beanNameNormalizer() {
            return new DefaultConditionalBeanRefBuilder();
        }

        @Bean
        public DefaultBeanMetaInfoExtractor beanAnalyzer(
                ConfigurableApplicationContext configurableApplicationContext,
                ConditionalBeanRefBuilder conditionalBeanRefBuilder) {
            return new DefaultBeanMetaInfoExtractor(configurableApplicationContext, conditionalBeanRefBuilder);
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
}
