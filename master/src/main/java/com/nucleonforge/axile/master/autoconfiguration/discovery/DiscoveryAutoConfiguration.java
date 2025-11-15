package com.nucleonforge.axile.master.autoconfiguration.discovery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.master.service.discovery.AxileKubernetesDiscoveryClient;
import com.nucleonforge.axile.master.service.discovery.InstancesDiscoverer;
import com.nucleonforge.axile.master.service.discovery.InstancesRegistrar;
import com.nucleonforge.axile.master.service.discovery.KubernetesInstanceDiscoverer;
import com.nucleonforge.axile.master.service.discovery.ShortPollingInstanceDiscoveryScheduler;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.ManagedServiceMetadataEndpointProber;

/**
 * Auto-configuration for K8S related components.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "axile.master.discovery", name = "auto", havingValue = "true")
public class DiscoveryAutoConfiguration {

    @Bean
    public InstancesRegistrar instancesRegistrar(
            InstancesDiscoverer instancesDiscoverer, InstanceRegistry instanceRegistry) {
        return new InstancesRegistrar(instancesDiscoverer, instanceRegistry);
    }

    @Bean
    public ShortPollingInstanceDiscoveryScheduler shortPollingInstanceDiscoveryScheduler(
            InstancesDiscoverer instancesDiscoverer, InstanceRegistry instanceRegistry) {
        return new ShortPollingInstanceDiscoveryScheduler(instancesDiscoverer, instanceRegistry);
    }

    @AutoConfiguration
    @ConditionalOnProperty(prefix = "axile.master.discovery", name = "platform", havingValue = "kubernetes")
    static class KubernetesDiscoveryAutoConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "axile.master.discovery.kubernetes")
        public KubernetesDiscoveryProperties kubernetesDiscoveryProperties() {
            return new KubernetesDiscoveryProperties();
        }

        @Bean
        public KubernetesClient kubernetesClient(KubernetesDiscoveryProperties kubernetesDiscoveryProperties)
                throws IOException {
            return new KubernetesClientBuilder()
                    .withConfig(new ConfigBuilder()
                            .withMasterUrl(kubernetesDiscoveryProperties.getKubeApiserverUrl())
                            // TODO: For some reason caCert it is not yet working
                            .withCaCertFile(kubernetesDiscoveryProperties.getCaCertFile())
                            .withOauthToken(Files.readString(Paths.get(kubernetesDiscoveryProperties.getTokenPath())))
                            .build())
                    .build();
        }

        @Bean
        public DiscoveryClient discoveryClient(
                KubernetesClient kubernetesClient, KubernetesDiscoveryProperties kubernetesDiscoveryProperties) {
            // TODO: Implement filtering by labels
            return new AxileKubernetesDiscoveryClient(
                    kubernetesClient, kubernetesDiscoveryProperties.getFilters().getNamespaces());
        }

        @Bean
        public KubernetesInstanceDiscoverer kubernetesInstanceDiscoverer(
                DiscoveryClient discoveryClient,
                ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber) {
            return new KubernetesInstanceDiscoverer(discoveryClient, managedServiceMetadataEndpointProber);
        }
    }
}
