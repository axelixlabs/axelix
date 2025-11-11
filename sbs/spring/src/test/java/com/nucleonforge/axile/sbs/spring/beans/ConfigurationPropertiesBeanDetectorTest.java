package com.nucleonforge.axile.sbs.spring.beans;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.nucleonforge.axile.sbs.spring.beans.ConfigurationPropertiesBeanDetector.isConfigurationPropertiesBean;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * Integration test for {@link ConfigurationPropertiesBeanDetector}.
 *
 * @since 10.11.2025
 * @author Nikita Kirillov
 */
@SpringBootTest
@Import({ConfigurationPropertiesBeanDetectorTest.ConfigurationPropertiesDeclarations.class})
class ConfigurationPropertiesBeanDetectorTest {

    @Autowired
    private ConfigurableApplicationContext context;

    @Test
    void shouldDetectConfigurationPropertiesBeans() {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        assertSoftly(sa -> {
            sa.assertThat(isConfigurationPropertiesBean(beanFactory, "directConfigProps"))
                    .isTrue();
            sa.assertThat(isConfigurationPropertiesBean(beanFactory, "configPropsWithPrefix"))
                    .isTrue();
            sa.assertThat(isConfigurationPropertiesBean(beanFactory, "regularService"))
                    .isFalse();
            sa.assertThat(isConfigurationPropertiesBean(beanFactory, "regularComponent"))
                    .isFalse();
            sa.assertThat(isConfigurationPropertiesBean(beanFactory, "abstractConfigProps"))
                    .isFalse();
        });
    }

    @Test
    void shouldHandleEdgeCases() {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        assertSoftly(sa -> {
            sa.assertThat(isConfigurationPropertiesBean(beanFactory, "nonExistentBean"))
                    .isFalse();
            sa.assertThat(isConfigurationPropertiesBean(beanFactory, "")).isFalse();
            sa.assertThat(isConfigurationPropertiesBean(beanFactory, "   ")).isFalse();
        });
    }

    @TestConfiguration
    static class ConfigurationPropertiesDeclarations {

        @ConfigurationProperties
        @Component("directConfigProps")
        static class DirectConfigurationProperties {}

        @ConfigurationProperties(prefix = "app")
        @Component("configPropsWithPrefix")
        static class ConfigurationPropertiesWithPrefix {}

        abstract static class AbstractConfigurationProperties {}

        @Bean
        public AbstractConfigurationProperties abstractConfigProps() {
            return new AbstractConfigurationProperties() {};
        }

        @Service("regularService")
        static class RegularService {
            public void doSomething() {}
        }

        @Component("regularComponent")
        static class RegularComponent {}
    }
}
