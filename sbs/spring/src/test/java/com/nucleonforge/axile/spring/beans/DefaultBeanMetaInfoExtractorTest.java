package com.nucleonforge.axile.spring.beans;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
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

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.api.BeansFeed.ComponentVariant;

import static com.nucleonforge.axile.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.CUSTOM_DATABASE_QUALIFIER_BEAN;
import static com.nucleonforge.axile.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.LAZY_COMPONENT;
import static com.nucleonforge.axile.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.LAZY_PRIMARY_BEAN_METHOD;
import static com.nucleonforge.axile.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.PRIMARY_COMPONENT;
import static com.nucleonforge.axile.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.QUALIFIED_BEAN_METHOD;
import static com.nucleonforge.axile.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.QUALIFIED_COMPONENT;
import static com.nucleonforge.axile.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.REGULAR_COMPONENT;
import static com.nucleonforge.axile.spring.beans.DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.SPRING_DATA_REPOSITORY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultBeanMetaInfoExtractor}.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
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
            assertThat(it.qualifiers()).isEmpty();
            assertThat(it.beanSource()).isInstanceOf(ComponentVariant.class);
        });
    }

    @Test
    void shouldAnalyzeLazyServiceBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(LAZY_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isTrue();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.qualifiers()).isEmpty();
            assertThat(it.beanSource()).isInstanceOf(ComponentVariant.class);
        });
    }

    @Test
    void shouldAnalyzePrimaryComponentBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(PRIMARY_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(response -> {
            assertThat(response.isLazyInit()).isFalse();
            assertThat(response.isPrimary()).isTrue();
            assertThat(response.qualifiers()).isEmpty();
            assertThat(response.beanSource()).isInstanceOf(ComponentVariant.class);
        });
    }

    @Test
    void shouldAnalyzeQualifiedServiceBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(QUALIFIED_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(response -> {
            assertThat(response.isLazyInit()).isFalse();
            assertThat(response.isPrimary()).isFalse();
            assertThat(response.beanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(response.qualifiers()).contains(QUALIFIED_COMPONENT);
        });
    }

    @Test
    void shouldAnalyzeLazyPrimaryBeanMethod() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(LAZY_PRIMARY_BEAN_METHOD, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(response -> {
            assertThat(response.isLazyInit()).isTrue();
            assertThat(response.isPrimary()).isTrue();
            assertThat(response.qualifiers()).isEmpty();
            assertThat(response.beanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat((BeansFeed.BeanMethod) response.beanSource()).satisfies(it -> {
                assertThat(it.enclosingClassName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getName());
                assertThat(it.methodName()).isEqualTo("lazyPrimaryBean");
            });
        });
    }

    @Test
    void shouldAnalyzeQualifiedBeanMethod() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(QUALIFIED_BEAN_METHOD, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(response -> {
            assertThat(response.isLazyInit()).isFalse();
            assertThat(response.isPrimary()).isFalse();
            assertThat(response.qualifiers()).contains(QUALIFIED_BEAN_METHOD);
            assertThat(response.beanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat((BeansFeed.BeanMethod) response.beanSource()).satisfies(it -> {
                assertThat(it.enclosingClassName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getName());
                assertThat(it.methodName()).isEqualTo(QUALIFIED_BEAN_METHOD);
            });
        });
    }

    @Test
    void shouldAnalyzeSpringDataRepositoryBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(SPRING_DATA_REPOSITORY, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(response -> {
            assertThat(response.isLazyInit()).isFalse();
            assertThat(response.isPrimary()).isFalse();
            assertThat(response.qualifiers()).isEmpty();
            assertThat(response.beanSource()).isInstanceOf(BeansFeed.FactoryBean.class);
            assertThat((BeansFeed.FactoryBean) response.beanSource()).satisfies(it -> {
                assertThat(it.factoryBeanName()).isEqualTo(JpaRepositoryFactoryBean.class.getName());
            });
        });
    }

    @Test
    void shouldDetectCustomQualifierAnnotations() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(CUSTOM_DATABASE_QUALIFIER_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.qualifiers()).contains(CUSTOM_DATABASE_QUALIFIER_BEAN);
            assertThat(it.beanSource()).isInstanceOf(ComponentVariant.class);
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

        static final String SPRING_DATA_REPOSITORY = "MyRepository";

        static final String CUSTOM_DATABASE_QUALIFIER_BEAN = "CustomDatabaseQualifierBean";

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
        public DefaultBeanMetaInfoExtractor beanAnalyzer(ConfigurableListableBeanFactory beanFactory) {
            return new DefaultBeanMetaInfoExtractor(beanFactory);
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

        @Service(CUSTOM_DATABASE_QUALIFIER_BEAN)
        @CustomDatabaseQualifier
        static class CustomDatabaseQualifierBean {}
    }
}
