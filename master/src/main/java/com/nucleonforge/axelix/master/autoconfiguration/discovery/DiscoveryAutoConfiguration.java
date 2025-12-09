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
package com.nucleonforge.axelix.master.autoconfiguration.discovery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.nucleonforge.axelix.master.service.discovery.docker.DockerDiscoveryClient;
import com.nucleonforge.axelix.master.service.discovery.docker.DockerInstanceDiscoverer;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.apache.commons.lang3.SystemUtils;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.common.domain.AxelixVersionDiscoverer;
import com.nucleonforge.axelix.master.service.MemoryUsageCache;
import com.nucleonforge.axelix.master.service.discovery.InstancesDiscoverer;
import com.nucleonforge.axelix.master.service.discovery.KubernetesDiscoveryClient;
import com.nucleonforge.axelix.master.service.discovery.KubernetesInstanceDiscoverer;
import com.nucleonforge.axelix.master.service.discovery.ShortPollingInstanceDiscoveryScheduler;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;
import com.nucleonforge.axelix.master.service.transport.ManagedServiceMetadataEndpointProber;

/**
 * Auto-configuration for K8S related components.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "axelix.master.discovery", name = "auto", havingValue = "true")
public class DiscoveryAutoConfiguration {

    @Bean
    public ShortPollingInstanceDiscoveryScheduler shortPollingInstanceDiscoveryScheduler(
            InstancesDiscoverer instancesDiscoverer,
            InstanceRegistry instanceRegistry,
            MemoryUsageCache memoryUsageCache) {
        return new ShortPollingInstanceDiscoveryScheduler(instancesDiscoverer, instanceRegistry, memoryUsageCache);
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
                AxelixVersionDiscoverer axelixVersionDiscoverer) {
            return new KubernetesInstanceDiscoverer(
                    discoveryClient, managedServiceMetadataEndpointProber, axelixVersionDiscoverer);
        }
    }

    @AutoConfiguration
    @ConditionalOnProperty(prefix = "axile.master.discovery", name = "platform", havingValue = "docker")
    static class DockerDiscoveryAutoConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "axile.master.discovery.docker")
        public DockerDiscoveryProperties dockerDiscoveryProperties() {
            return new DockerDiscoveryProperties();
        }

        @Bean
        public DockerClient dockerClient() {
            // We may encounter issues described in this article
            // https://javanexus.com/blog/docker-rest-api-issues-java-developers
            DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();

            // TODO: We cannot guarantee that this code will work on Windows OS
            String envDockerHost = System.getenv("DOCKER_HOST");
            if (envDockerHost != null && !envDockerHost.isBlank()) {
                builder.withDockerHost(envDockerHost);
            } else if (SystemUtils.IS_OS_WINDOWS) {
                builder.withDockerHost("npipe:////./pipe/docker_engine");
            } else {
                builder.withDockerHost("unix:///var/run/docker.sock");
            }

            DockerClientConfig config = builder.build();

            ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .maxConnections(200)
                .build();

            return DockerClientImpl.getInstance(config, httpClient);
        }

        @Bean
        public DiscoveryClient dockerDiscoveryClient(
            DockerClient dockerClient, DockerDiscoveryProperties dockerDiscoveryProperties) {
            return new DockerDiscoveryClient(dockerClient, dockerDiscoveryProperties.getFilters());
        }

        @Bean
        public DockerInstanceDiscoverer dockerInstanceDiscoverer(
            DiscoveryClient discoveryClient,
            ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber) {
            return new DockerInstanceDiscoverer(discoveryClient, managedServiceMetadataEndpointProber);
        }
    }
}
