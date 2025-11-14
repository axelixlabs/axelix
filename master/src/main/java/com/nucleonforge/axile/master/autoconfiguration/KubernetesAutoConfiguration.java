package com.nucleonforge.axile.master.autoconfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClientAutoConfiguration;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryProperties;
import org.springframework.cloud.kubernetes.fabric8.discovery.KubernetesDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.nucleonforge.axile.master.service.discovery.AxileKubernetesDiscoveryClient;

/**
 * Auto-configuration for K8S related components.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
@AutoConfigureBefore(KubernetesDiscoveryClientAutoConfiguration.class)
@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
@EnableConfigurationProperties({KubernetesDiscoveryProperties.class})
@Import(KubernetesAutoConfiguration.KubernetesExclusionConfiguration.class)
public class KubernetesAutoConfiguration {

    // TODO:
    //  we need to think about the design of properties for Axile in general. Here, we're expecting the
    //  access token to be provided as a properties inside the spring.cloud.kubernetes namespace, which
    //  is probably fine for now, but we do not ahe a defined policy as of now.
    @Bean
    public KubernetesClient kubernetesClient(
            @Value("${spring.cloud.kubernetes.discovery.discovery-server-url}") String masterUrl,
            @Value("${spring.cloud.kubernetes.sa-token-path}") String tokenPath,
            // TODO: we're not using this caCert now because for some reason it is not yet working
            @Value("${spring.cloud.kubernetes.ca-cert-path}") String caCertFile)
            throws IOException {
        return new KubernetesClientBuilder()
                .withConfig(new ConfigBuilder()
                        .withMasterUrl(masterUrl)
                        //                                                .withCaCertFile(caCertFile)
                        .withOauthToken(Files.readString(Paths.get(tokenPath)))
                        .build())
                .build();
    }

    @Bean
    public DiscoveryClient discoveryClient(
            KubernetesClient kubernetesClient, KubernetesDiscoveryProperties discoveryProperties) {
        return new AxileKubernetesDiscoveryClient(kubernetesClient, discoveryProperties.namespaces());
    }

    /**
     * Explicitly excludes Spring Boot's composite discovery client auto-configuration.
     */
    @Configuration
    @EnableAutoConfiguration(exclude = CompositeDiscoveryClientAutoConfiguration.class)
    static class KubernetesExclusionConfiguration {}
}
