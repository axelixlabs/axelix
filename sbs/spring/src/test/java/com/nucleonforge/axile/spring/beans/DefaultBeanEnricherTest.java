//package com.nucleonforge.axile.spring.beans;
//
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//import java.util.Optional;
//
//import javax.sql.DataSource;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.EntityManagerFactory;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//
//import org.junit.jupiter.api.Test;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
//import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.JpaVendorAdapter;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Repository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Test class for verifying the functionality of {@link DefaultBeanEnricher}.
// *
// * @since 07.07.2025
// * @author Nikita Kirillov
// */
//@SpringBootTest(classes = DefaultBeanEnricherTest.DefaultBeanAnalyzerTestConfig.class)
//class DefaultBeanEnricherTest {
//
//    @Autowired
//    private BeanEnricher analyzer;
//
//    @Test
//    void shouldAnalyzeServiceBean() {
//        Optional<BeanProfile> optResponse = analyzer.analyze("myService");
//
//        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
//            assertThat(response.isLazyInit()).isFalse();
//            assertThat(response.isPrimary()).isFalse();
//            assertThat(response.qualifiers()).isEmpty();
//            assertThat(response.enclosingClassName())
//                    .isEqualTo(DefaultBeanAnalyzerTestConfig.MyService.class.getName());
//            assertThat(response.methodName()).isNull();
//            assertThat(response.factoryBeanName()).isNull();
//        });
//    }
//
//    @Test
//    void shouldAnalyzeLazyServiceBean() {
//        Optional<BeanProfile> optResponse = analyzer.analyze("lazyService");
//
//        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
//            assertThat(response.isLazyInit()).isTrue();
//            assertThat(response.isPrimary()).isFalse();
//            assertThat(response.qualifiers()).isEmpty();
//            assertThat(response.enclosingClassName())
//                    .isEqualTo(DefaultBeanAnalyzerTestConfig.LazyService.class.getName());
//            assertThat(response.methodName()).isNull();
//            assertThat(response.factoryBeanName()).isNull();
//        });
//    }
//
//    @Test
//    void shouldAnalyzePrimaryComponentBean() {
//        Optional<BeanProfile> optResponse = analyzer.analyze("primaryComponent");
//
//        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
//            assertThat(response.isLazyInit()).isFalse();
//            assertThat(response.isPrimary()).isTrue();
//            assertThat(response.qualifiers()).isEmpty();
//            assertThat(response.enclosingClassName())
//                    .isEqualTo(DefaultBeanAnalyzerTestConfig.PrimaryComponent.class.getName());
//            assertThat(response.methodName()).isNull();
//            assertThat(response.factoryBeanName()).isNull();
//        });
//    }
//
//    @Test
//    void shouldAnalyzeQualifiedServiceBean() {
//        Optional<BeanProfile> optResponse = analyzer.analyze("qualifiedService");
//
//        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
//            assertThat(response.isLazyInit()).isFalse();
//            assertThat(response.isPrimary()).isFalse();
//            assertThat(response.qualifiers()).contains("specialService");
//            assertThat(response.enclosingClassName())
//                    .isEqualTo(DefaultBeanAnalyzerTestConfig.QualifiedService.class.getName());
//            assertThat(response.methodName()).isNull();
//            assertThat(response.factoryBeanName()).isNull();
//        });
//    }
//
//    @Test
//    void shouldAnalyzeLazyPrimaryBeanMethod() {
//        Optional<BeanProfile> optResponse = analyzer.analyze("lazyPrimaryBean");
//
//        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
//            assertThat(response.isLazyInit()).isTrue();
//            assertThat(response.isPrimary()).isTrue();
//            assertThat(response.qualifiers()).isEmpty();
//            assertThat(response.enclosingClassName())
//                    .isEqualTo("defaultBeanAnalyzerTest.DefaultBeanAnalyzerTestConfig");
//            assertThat(response.methodName()).isEqualTo("lazyPrimaryBean");
//            assertThat(response.factoryBeanName()).isEqualTo("defaultBeanAnalyzerTest.DefaultBeanAnalyzerTestConfig");
//        });
//    }
//
//    @Test
//    void shouldAnalyzeQualifiedBeanMethod() {
//        Optional<BeanProfile> optResponse = analyzer.analyze("qualifiedBeanMethod");
//
//        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
//            assertThat(response.isLazyInit()).isFalse();
//            assertThat(response.isPrimary()).isFalse();
//            assertThat(response.qualifiers()).contains("customQualifier");
//            assertThat(response.enclosingClassName())
//                    .isEqualTo("defaultBeanAnalyzerTest.DefaultBeanAnalyzerTestConfig");
//            assertThat(response.methodName()).isEqualTo("qualifiedBeanMethod");
//            assertThat(response.factoryBeanName()).isEqualTo("defaultBeanAnalyzerTest.DefaultBeanAnalyzerTestConfig");
//        });
//    }
//
//    @Test
//    void shouldAnalyzeRepositoryBean() {
//        Optional<BeanProfile> optResponse =
//                analyzer.analyze("defaultBeanAnalyzerTest.DefaultBeanAnalyzerTestConfig.MyRepository");
//
//        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
//            assertThat(response.isLazyInit()).isFalse();
//            assertThat(response.isPrimary()).isFalse();
//            assertThat(response.qualifiers()).isEmpty();
//            assertThat(response.enclosingClassName()).isNotNull();
//            assertThat(response.methodName()).isNull();
//            assertThat(response.factoryBeanName()).isNotNull();
//        });
//    }
//
//    @Test
//    void shouldAnalyzeDefaultBeanAnalyzer() {
//        Optional<BeanProfile> optResponse = analyzer.analyze("defaultBeanAnalyzer");
//
//        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
//            assertThat(response.isLazyInit()).isFalse();
//            assertThat(response.isPrimary()).isFalse();
//            assertThat(response.qualifiers()).isEmpty();
//            assertThat(response.enclosingClassName())
//                    .isEqualTo("defaultBeanAnalyzerTest.DefaultBeanAnalyzerTestConfig");
//            assertThat(response.methodName()).isEqualTo("beanAnalyzer");
//            assertThat(response.factoryBeanName()).isEqualTo("defaultBeanAnalyzerTest.DefaultBeanAnalyzerTestConfig");
//        });
//    }
//
//    @Test
//    void shouldDetectCustomQualifierAnnotations() {
//        Optional<BeanProfile> optResponse = analyzer.analyze("customDatabaseService");
//
//        assertThat(optResponse).isPresent().hasValueSatisfying(response -> {
//            assertThat(response.qualifiers()).contains("customDatabase");
//            assertThat(response.enclosingClassName())
//                    .isEqualTo(DefaultBeanAnalyzerTestConfig.CustomDatabaseService.class.getName());
//        });
//    }
//
//    @Test
//    void shouldReturnEmptyForUnknownBean() {
//        Optional<BeanProfile> optResponse = analyzer.analyze("nonExistentBean");
//        assertThat(optResponse).isEmpty();
//    }
//
//    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
//    @Retention(RetentionPolicy.RUNTIME)
//    @Qualifier("customDatabase")
//    public @interface CustomDatabaseQualifier {}
//
//    /**
//     * Static nested configuration class for {@link DefaultBeanEnricherTest}.
//     */
//    @TestConfiguration
//    @EnableJpaRepositories(
//            basePackageClasses = DefaultBeanAnalyzerTestConfig.MyRepository.class,
//            considerNestedRepositories = true)
//    @EntityScan(basePackageClasses = DefaultBeanAnalyzerTestConfig.MyEntity.class)
//    public static class DefaultBeanAnalyzerTestConfig {
//
//        @Bean
//        public static QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
//            return new QualifiersPersistencePostProcessor();
//        }
//
//        @Bean
//        public DataSource dataSource() {
//            return new EmbeddedDatabaseBuilder()
//                    .setType(EmbeddedDatabaseType.H2)
//                    .build();
//        }
//
//        @Bean
//        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
//            LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
//            emf.setDataSource(dataSource);
//            emf.setPackagesToScan(DefaultBeanAnalyzerTestConfig.MyEntity.class.getPackageName());
//
//            JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
//            emf.setJpaVendorAdapter(vendorAdapter);
//
//            emf.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "create-drop");
//            return emf;
//        }
//
//        @Bean
//        public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
//            return new JpaTransactionManager(emf);
//        }
//
//        @Service("myService")
//        static class MyService {}
//
//        @Bean("defaultBeanAnalyzer")
//        public BeanEnricher beanAnalyzer(ApplicationContext context) {
//            return new DefaultBeanEnricher(context);
//        }
//
//        @Entity
//        @Table(name = "my_entity")
//        public static class MyEntity {
//            @Id
//            @GeneratedValue(strategy = GenerationType.AUTO)
//            private Long id;
//        }
//
//        @Repository
//        public interface MyRepository extends JpaRepository<MyEntity, Long> {}
//
//        @Service("lazyService")
//        @Lazy
//        static class LazyService {}
//
//        @Component("primaryComponent")
//        @Primary
//        static class PrimaryComponent {}
//
//        @Service("qualifiedService")
//        @Qualifier("specialService")
//        static class QualifiedService {}
//
//        @Bean
//        @Lazy
//        @Primary
//        public String lazyPrimaryBean() {
//            return "lazyPrimaryBean";
//        }
//
//        @Bean
//        @Qualifier("customQualifier")
//        public String qualifiedBeanMethod() {
//            return "qualifiedBeanMethod";
//        }
//
//        @Service("customDatabaseService")
//        @CustomDatabaseQualifier
//        static class CustomDatabaseService {}
//    }
//}
