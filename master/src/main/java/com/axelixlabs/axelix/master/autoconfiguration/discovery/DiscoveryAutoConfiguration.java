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
package com.axelixlabs.axelix.master.autoconfiguration.discovery;

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

import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.master.service.InstanceFactory;
import com.axelixlabs.axelix.master.service.InstanceRegistrar;
import com.axelixlabs.axelix.master.service.discovery.InstancesDiscoverer;
import com.axelixlabs.axelix.master.service.discovery.ShortPollingInstanceDiscoveryScheduler;
import com.axelixlabs.axelix.master.service.discovery.k8s.KubernetesDiscoveryClient;
import com.axelixlabs.axelix.master.service.discovery.k8s.KubernetesInstanceDiscoverer;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.ManagedServiceMetadataEndpointProber;

/**
 * Auto-configuration for K8S related components.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "axelix.master.discovery", name = "auto", havingValue = "true")
public class DiscoveryAutoConfiguration {

    @Bean
    public ShortPollingInstanceDiscoveryScheduler shortPollingInstanceDiscoveryScheduler(
            InstancesDiscoverer instancesDiscoverer,
            InstanceRegistrar instanceRegistrar,
            InstanceRegistry instanceRegistry) {
        return new ShortPollingInstanceDiscoveryScheduler(instancesDiscoverer, instanceRegistrar, instanceRegistry);
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
                ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber,
                AxelixVersionDiscoverer axelixVersionDiscoverer,
                InstanceFactory instanceFactory) {
            return new KubernetesInstanceDiscoverer(
                    discoveryClient, managedServiceMetadataEndpointProber, axelixVersionDiscoverer, instanceFactory);
        }
    }
}
