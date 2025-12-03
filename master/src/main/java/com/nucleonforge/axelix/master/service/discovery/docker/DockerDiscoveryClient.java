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
package com.nucleonforge.axelix.master.service.discovery.docker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ContainerNetworkSettings;
import com.github.dockerjava.api.model.ContainerPort;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;

import com.nucleonforge.axile.master.autoconfiguration.discovery.DockerDiscoveryProperties;

/**
 * Docker implementation of {@link DiscoveryClient} that discovers services in the same networks as this application.
 *
 * @author Sergey Cherkasov
 */
public class DockerDiscoveryClient implements DiscoveryClient {
    private static final Logger log = LoggerFactory.getLogger(DockerDiscoveryClient.class);

    private static final String ALLOWED_STATUS = "running";

    private final DockerClient dockerClient;
    private final Set<String> filters;

    public DockerDiscoveryClient(DockerClient dockerClient, DockerDiscoveryProperties.DiscoveryFilters filters) {
        this.dockerClient = dockerClient;
        this.filters = filters.getNetworksName();
    }

    @Override
    public String description() {
        return "Docker Discovery Client";
    }

    @Override
    public List<ServiceInstance> getInstances(@NonNull String serviceId) {
        List<Container> containers = getContainers();

        if (CollectionUtils.isEmpty(containers)) {
            return List.of();
        }

        String selfContainerId = getSelfContainerId(containers);
        Set<String> selfNetworksName = getNetworksName(selfContainerId);

        return containers.stream()
                .filter(container -> !container.getId().equals(selfContainerId))
                .filter(this::isAllowedCommand)
                .filter(container -> container.getState().equals(ALLOWED_STATUS))
                .filter(container -> isValidNetwork(container, selfNetworksName))
                .map(container -> createServiceInstance(container, selfNetworksName))
                .toList();
    }

    @Override
    public List<String> getServices() {
        List<Container> containers = getContainers();

        if (CollectionUtils.isEmpty(containers)) {
            return List.of();
        }

        String selfContainerId = getSelfContainerId(containers);
        Set<String> selfNetworksName = getNetworksName(selfContainerId);

        return containers.stream()
                .filter(c -> !c.getId().equals(selfContainerId))
                .filter(this::isAllowedCommand)
                .filter(c -> c.getState().equals(ALLOWED_STATUS))
                .filter(c -> isValidNetwork(c, selfNetworksName))
                .map(Container::getImage)
                .toList();
    }

    private List<Container> getContainers() {
        return dockerClient
                .listContainersCmd()
                .withShowAll(true)
                .withShowSize(true)
                .exec();
    }

    private boolean isValidNetwork(Container container, Set<String> networksName) {
        return Optional.ofNullable(container.getNetworkSettings())
                .map(ContainerNetworkSettings::getNetworks)
                .map(map -> map.keySet().stream().anyMatch(networksName::contains))
                .orElse(false);
    }

    private Set<String> getNetworksName(String containerId) {
        if (filters != null) {
            return filters;
        }

        return dockerClient
                .inspectContainerCmd(containerId)
                .exec()
                .getNetworkSettings()
                .getNetworks()
                .keySet();
    }

    private String getSelfContainerId(List<Container> containers) {
        try {
            String hostname = System.getenv("HOSTNAME");
            String ipAddress = InetAddress.getByName(hostname).getHostAddress();
            return containers.stream()
                    .filter(container -> {
                        Map<String, ContainerNetwork> networks = Optional.ofNullable(container.getNetworkSettings())
                                .map(ContainerNetworkSettings::getNetworks)
                                .orElse(Map.of());
                        return networks.values().stream().anyMatch(net -> ipAddress.equals(net.getIpAddress()));
                    })
                    .findFirst()
                    .map(Container::getId)
                    .orElseThrow(() -> new IllegalStateException("Cannot determine current container ID"));
        } catch (UnknownHostException e) {
            return "";
        }
    }

    private boolean isAllowedCommand(Container c) {
        String command = c.getCommand().toLowerCase();

        return command.matches(".*(^|\\s)java(\\s|$).*")
                || command.contains(" -jar ")
                || command.endsWith(".jar")
                || command.contains(".jar ");
    }

    private ServiceInstance createServiceInstance(Container container, Set<String> selfNetworksName) {
        return new DockerServiceInstance(
                container.getId(),
                container.getImageId() != null ? container.getImageId() : container.getId(),
                container.getImage(),
                getIpAddressContainer(container, selfNetworksName),
                getPortContainer(container),
                getCreatedAt(container));
    }

    private String getCreatedAt(Container container) {
        return Instant.ofEpochSecond(container.getCreated()).toString();
    }

    private int getPortContainer(Container container) {
        return Arrays.stream(container.getPorts())
                .map(ContainerPort::getPrivatePort)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Container '%s' has no exposed private ports or none of them are valid."
                                .formatted(container.getId())));
    }

    private String getIpAddressContainer(Container container, Set<String> selfNetworksName) {
        Map<String, ContainerNetwork> containerNets = Optional.ofNullable(container.getNetworkSettings())
                .map(ContainerNetworkSettings::getNetworks)
                .orElse(Map.of());

        return selfNetworksName.stream()
                .filter(containerNets::containsKey)
                .map(containerNets::get)
                .filter(Objects::nonNull)
                .map(ContainerNetwork::getIpAddress)
                .filter(ip -> ip != null && !ip.isBlank())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Container '%s' has no valid IP address.".formatted(container.getId())));
    }
}
