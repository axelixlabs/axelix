/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.master.autoconfiguration.discovery;

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

import com.nucleonforge.axelix.master.service.discovery.InstancesDiscoverer;
import com.nucleonforge.axelix.master.service.discovery.InstancesRegistrar;
import com.nucleonforge.axelix.master.service.discovery.KubernetesDiscoveryClient;
import com.nucleonforge.axelix.master.service.discovery.KubernetesInstanceDiscoverer;
import com.nucleonforge.axelix.master.service.discovery.ShortPollingInstanceDiscoveryScheduler;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;
import com.nucleonforge.axelix.master.service.transport.ManagedServiceMetadataEndpointProber;

/**
 * Auto-configuration for K8S related components.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "axelix.master.discovery", name = "auto", havingValue = "true")
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
    @ConditionalOnProperty(prefix = "axelix.master.discovery", name = "platform", havingValue = "kubernetes")
    static class KubernetesDiscoveryAutoConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "axelix.master.discovery.kubernetes")
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
                            .withCaCertFile(kubernetesDiscoveryProperties.getCaCertPath())
                            .withOauthToken(Files.readString(Paths.get(kubernetesDiscoveryProperties.getSaTokenPath())))
                            .build())
                    .build();
        }

        @Bean
        public DiscoveryClient discoveryClient(
                KubernetesClient kubernetesClient, KubernetesDiscoveryProperties kubernetesDiscoveryProperties) {
            return new KubernetesDiscoveryClient(kubernetesClient, kubernetesDiscoveryProperties.getFilters());
        }

        @Bean
        public KubernetesInstanceDiscoverer kubernetesInstanceDiscoverer(
                DiscoveryClient discoveryClient,
                ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber) {
            return new KubernetesInstanceDiscoverer(discoveryClient, managedServiceMetadataEndpointProber);
        }
    }
}
