package com.nucleonforge.axile.master.autoconfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryClient;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryClientAutoConfiguration;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Auto-configuration for K8S related components.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration(before = KubernetesDiscoveryClientAutoConfiguration.class)
@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
@EnableConfigurationProperties(KubernetesDiscoveryClientProperties.class)
public class KubernetesAutoConfiguration {

    @Bean
    public DiscoveryClient kubernetesDiscoveryClient(
            @K8SRestTemplate RestTemplate restTemplate, KubernetesDiscoveryClientProperties properties) {
        return new KubernetesDiscoveryClient(restTemplate, properties);
    }

    // TODO:
    //  we need to think about the design of properties for Axile in general. Here, we're expecting the
    //  access token to be provided as a properties inside the spring.cloud.kubernetes namespace, which
    //  is probably fine for now, but we do not ahe a defined policy as of now.

    /**
     * @implNote see <a href="https://kubernetes.io/docs/tasks/run-application/access-api-from-pod/">Kubernetes docs</a>
     */
    @Bean
    @K8SRestTemplate
    public RestTemplate k8sRestTemplate(@Value("${spring.cloud.kubernetes.sa-token-path}") String saTokenPath) {
        return new RestTemplateBuilder()
                .interceptors(
                        new KubernetesSATokenHttpRequestInterceptor(saTokenPath),
                        new ControlPlainHttpRequestsLoggingInterceptor())
                .build();
    }

    /**
     * Qualifier annotation ot mark the {@link RestTemplate} to be used for K8S service discovery capabilities.
     */
    @Qualifier
    @Target({ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @interface K8SRestTemplate {}
}
