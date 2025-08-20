package com.nucleonforge.axile.sbs.k8s;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ConditionalOnK8SEnvironment}.
 *
 * @author Nikita Kirillov
 * @since 21.08.2025
 */
@SpringBootTest(classes = ConditionalOnK8SEnvironmentTest.OnMethodConditionConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ConditionalOnK8SEnvironmentTest {

    @Nested
    @SetEnvironmentVariable(key = "KUBERNETES_SERVICE_HOST", value = "10.96.0.1")
    @SetEnvironmentVariable(key = "KUBERNETES_SERVICE_PORT", value = "443")
    class WhenKubernetesEnvironmentVariablesAreSet {

        @Test
        void shouldLoadKubernetesBean(ApplicationContext context) {
            assertThat(context.containsBean("kubernetesBean")).isTrue();
        }
    }

    @Nested
    @SetEnvironmentVariable(key = "APPLICATION_SERVICE_HOST", value = "10.96.0.1")
    @SetEnvironmentVariable(key = "APPLICATION_SERVICE_PORT", value = "443")
    class WhenApplicationServiceEnvironmentVariablesAreSet {

        @Test
        void shouldLoadKubernetesConfigurationWithK8sProfile(ApplicationContext context) {
            assertThat(context.containsBean("kubernetesBean")).isTrue();
        }
    }

    @Nested
    @TestPropertySource(properties = {"spring.main.cloud-platform=kubernetes"})
    class WhenCloudPlatformIsKubernetes {

        @Test
        void shouldLoadKubernetesBean(ApplicationContext context) {
            assertThat(context.containsBean("kubernetesBean")).isTrue();
        }
    }

    @Nested
    @TestPropertySource(properties = {"spring.main.cloud-platform=none"})
    class WhenCloudPlatformIsNone {

        @Test
        void shouldNotLoadKubernetesBean(ApplicationContext context) {
            assertThat(context.containsBean("alwaysActiveBean")).isTrue();

            assertThat(context.containsBean("kubernetesBean")).isFalse();

            assertThatThrownBy(() -> context.getBean("kubernetesBean"))
                    .isInstanceOf(NoSuchBeanDefinitionException.class);
        }
    }

    @Nested // running without any K8s environment configuration
    class WhenNoKubernetesEnvironmentDetected {

        @Test
        void shouldNotLoadConfiguration(ApplicationContext context) {
            assertThat(context.containsBean("kubernetesBean")).isFalse();
            assertThat(context.containsBean("alwaysActiveBean")).isTrue();
        }
    }

    @Nested
    @TestPropertySource(properties = "spring.main.cloud-platform=cloud_foundry")
    class WhenRunningOnCloudFoundry {

        @Test
        void shouldNotLoadKubernetesBeans(ApplicationContext context) {
            assertThat(context.containsBean("kubernetesBean")).isFalse();
        }
    }

    @Nested
    @TestPropertySource(properties = "spring.main.cloud-platform=heroku")
    class WhenRunningOnHeroku {

        @Test
        void shouldNotLoadKubernetesBeans(ApplicationContext context) {
            assertThat(context.containsBean("kubernetesBean")).isFalse();
        }
    }

    @SpringBootConfiguration
    static class OnMethodConditionConfiguration {

        @Bean
        @ConditionalOnK8SEnvironment
        public String kubernetesBean() {
            return "kubernetes-active";
        }

        @Bean
        public String alwaysActiveBean() {
            return "always-active";
        }
    }
}
