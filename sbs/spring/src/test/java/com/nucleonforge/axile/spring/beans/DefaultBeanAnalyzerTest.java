package com.nucleonforge.axile.spring.beans;

import java.util.Optional;

import javax.sql.DataSource;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for verifying the functionality of {@link DefaultBeanAnalyzer}.
 * <p>
 * Uses the {@link DefaultBeanAnalyzerTestConfig} configuration which defines
 * necessary beans including JPA repository, services, and prototype/request
 * scoped beans for various scenarios.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = DefaultBeanAnalyzerTest.DefaultBeanAnalyzerTestConfig.class)
class DefaultBeanAnalyzerTest {

    @Autowired
    private BeanAnalyzer analyzer;

    @Test
    void shouldAnalyzeServiceBean() {
        Optional<BeanProfile> optResponse = analyzer.analyze("myService");

        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
            assertThat(response.beanClass()).isNotNull();
            assertThat(response.beanClass().getSimpleName()).isEqualTo("MyService");

            assertThat(response)
                    .returns("singleton", BeanProfile::scope)
                    .returns(null, BeanProfile::definingMethod)
                    .returns(false, BeanProfile::factoryBean);
        });
    }

    @Test
    void shouldAnalyzeCustomMethod() {
        Optional<BeanProfile> optResponse = analyzer.analyze("customBean");

        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
            assertThat(response.definingMethod().getName()).isEqualTo("customBean");
            assertThat(response.beanClass().getSimpleName()).isEqualTo("DefaultBeanAnalyzerTestConfig");

            assertThat(response).returns("singleton", BeanProfile::scope).returns(true, BeanProfile::factoryBean);
        });
    }

    @Test
    void shouldAnalyzeRepositoryBean() {
        Optional<BeanProfile> optResponse =
                analyzer.analyze("defaultBeanAnalyzerTest.DefaultBeanAnalyzerTestConfig.MyRepository");

        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
            assertThat(response.beanClass()).isNotNull();
            assertThat(response.beanClass().getSimpleName()).contains("JpaRepositoryFactoryBean");

            assertThat(response)
                    .returns("singleton", BeanProfile::scope)
                    .returns(false, BeanProfile::factoryBean)
                    .returns(null, BeanProfile::definingMethod);
        });
    }

    @Test
    void shouldReturnEmptyForUnknownBean() {
        Optional<BeanProfile> optResponse = analyzer.analyze("nonExistentBean");
        assertThat(optResponse).isEmpty();
    }

    @Test
    void shouldAnalyzeDefaultBeanAnalyzer() {
        Optional<BeanProfile> optResponse = analyzer.analyze("defaultBeanAnalyzer");

        assertThat(optResponse).isPresent().hasValueSatisfying(response -> assertThat(response)
                .returns("singleton", BeanProfile::scope)
                .returns(true, BeanProfile::factoryBean));
    }

    @Test
    void shouldAnalyzePrototypeBean() {
        Optional<BeanProfile> optResponse = analyzer.analyze("myPrototypeBean");

        assertThat(optResponse).isPresent().hasValueSatisfying(response -> assertThat(response)
                .returns("prototype", BeanProfile::scope)
                .returns(true, BeanProfile::factoryBean));
    }

    @Test
    void shouldAnalyzeRequestBean() {
        Optional<BeanProfile> optResponse = analyzer.analyze("myRequestBean");

        assertThat(optResponse).isPresent().hasValueSatisfying(response -> assertThat(response)
                .returns("request", BeanProfile::scope)
                .returns(true, BeanProfile::factoryBean));
    }

    /**
     * Static nested configuration class for {@link DefaultBeanAnalyzerTest}.
     * <p>
     * This configuration supports testing of bean analysis across different bean
     * scopes, factory methods, and JPA repository beans.
     */
    @TestConfiguration
    @EnableJpaRepositories(
            basePackageClasses = DefaultBeanAnalyzerTestConfig.MyRepository.class,
            considerNestedRepositories = true)
    @EntityScan(basePackageClasses = DefaultBeanAnalyzerTestConfig.MyEntity.class)
    public static class DefaultBeanAnalyzerTestConfig {

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
            emf.setPackagesToScan(DefaultBeanAnalyzerTestConfig.MyEntity.class.getPackageName());

            JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            emf.setJpaVendorAdapter(vendorAdapter);

            emf.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "create-drop");
            return emf;
        }

        @Bean
        public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }

        @Service("myService")
        static class MyService {}

        @Bean
        public String customBean() {
            return "customBean";
        }

        @Bean
        @Scope("prototype")
        public String myPrototypeBean() {
            return "myPrototypeBean";
        }

        @Bean
        @Scope("request")
        public String myRequestBean() {
            return "myRequestBean";
        }

        @Bean("defaultBeanAnalyzer")
        public BeanAnalyzer beanAnalyzer(ApplicationContext context) {
            return new DefaultBeanAnalyzer(context);
        }

        @Entity
        @Table(name = "my_entity")
        public static class MyEntity {
            @Id
            @GeneratedValue(strategy = GenerationType.AUTO)
            private Long id;
        }

        @Repository
        public interface MyRepository extends JpaRepository<MyEntity, Long> {}
    }
}
