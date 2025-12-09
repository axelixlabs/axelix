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

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ContainerNetworkSettings;
import com.github.dockerjava.api.model.ContainerPort;
import org.jspecify.annotations.NonNull;

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
    private static final String ALLOWED_STATUS = "running";
    private static final String COMPOSE_SERVICE = "com.docker.compose.service";
    private static final String HOSTNAME = "HOSTNAME";

    private final DockerClient dockerClient;
    private final DockerDiscoveryProperties.DiscoveryFilters filters;

    public DockerDiscoveryClient(DockerClient dockerClient, DockerDiscoveryProperties.DiscoveryFilters filters) {
        this.dockerClient = dockerClient;
        this.filters = filters;
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
        Set<String> selfNetworksName = getSelfNetworksName(selfContainerId);

        return buildInstances(serviceId, selfContainerId, containers, selfNetworksName);
    }

    @Override
    public List<String> getServices() {
        List<Container> containers = getContainers();

        if (CollectionUtils.isEmpty(containers)) {
            return List.of();
        }

        String selfContainerId = getSelfContainerId(containers);
        Set<String> selfNetworksName = getSelfNetworksName(selfContainerId);

        return containers.stream()
                .filter(container -> !container.getId().equals(selfContainerId))
                .filter(this::isAllowedCommand)
                .filter(container -> container.getState().equals(ALLOWED_STATUS))
                .filter(container -> isValidNetwork(container, selfNetworksName))
                .map(container -> container.getLabels().getOrDefault(COMPOSE_SERVICE, container.getImage()))
                .toList();
    }

    private List<Container> getContainers() {
        return dockerClient.listContainersCmd().withShowAll(true).exec();
    }

    // We need to select only those Docker containers that are actually running a JVM.
    private boolean isAllowedCommand(Container container) {
        String command = container.getCommand().toLowerCase();

        return command.matches(".*(^|\\s)java(\\s|$).*")
                || command.contains(" -jar ")
                || command.endsWith(".jar")
                || command.contains(".jar ");
    }

    private boolean isValidNetwork(Container container, Set<String> networksName) {
        return Optional.ofNullable(container.getNetworkSettings())
                .map(ContainerNetworkSettings::getNetworks)
                .map(map -> map.keySet().stream().anyMatch(networksName::contains))
                .orElse(false);
    }

    // TODO: We cannot guarantee that this code will work on Windows OS
    // We implemented two mechanisms for obtaining the container ID of the JVM’s host container.
    // These approaches should be revisited in the future to establish a more reliable solution.
    private String getSelfContainerId(List<Container> containers) {
        try {
            Optional<String> cgroupId = Files.lines(Paths.get("/proc/self/cgroup"))
                    .map(line -> line.replaceAll(".*\\/docker\\/", ""))
                    .filter(id -> id.length() == 64)
                    .findFirst();
            if (cgroupId.isPresent()) {
                return cgroupId.get();
            }

            String hostname = System.getenv(HOSTNAME);
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
                    .orElseThrow(() -> new IllegalStateException(
                            "It is impossible to determine the container ID on which the JVM is running"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // We need to obtain the networks within which the JVM can communicate with other services
    // if the networks are not specified in the Environment properties.
    private Set<String> getSelfNetworksName(String containerId) {
        if (filters != null && filters.getNetworksName() != null) {
            return filters.getNetworksName();
        }

        return dockerClient
                .inspectContainerCmd(containerId)
                .exec()
                .getNetworkSettings()
                .getNetworks()
                .keySet();
    }

    private List<ServiceInstance> buildInstances(
            String serviceId, String selfContainerId, List<Container> containers, Set<String> selfNetworksName) {
        return containers.stream()
                .filter(container -> !container.getId().equals(selfContainerId))
                .filter(this::isAllowedCommand)
                .filter(container -> container.getState().equals(ALLOWED_STATUS))
                .filter(container -> container
                        .getLabels()
                        .getOrDefault(COMPOSE_SERVICE, container.getImage())
                        .equals(serviceId))
                .map(container -> createServiceInstance(serviceId, container, selfNetworksName))
                .toList();
    }

    private ServiceInstance createServiceInstance(String serviceId, Container container, Set<String> selfNetworksName) {
        String name = container.getNames()[0].replaceFirst("^/", "");
        Map.Entry<String, String> networkInfo = getNetworkInfo(serviceId, container, selfNetworksName);
        Map.Entry<String, Integer> portInfo = getPortInfo(serviceId, container);

        Map<String, String> metadata = Map.of(
                "networkName", networkInfo.getKey(),
                "servicePortName", container.getNames()[0],
                "protocol", portInfo.getKey());

        return new DockerServiceInstance(
                container.getId(),
                container.getLabels().getOrDefault(COMPOSE_SERVICE, container.getImage()),
                name,
                networkInfo.getValue(),
                portInfo.getValue(),
                false,
                metadata,
                getCreatedAt(container));
    }

    private Map.Entry<String, String> getNetworkInfo(String serviceId, Container container, Set<String> networksName) {
        Map<String, ContainerNetwork> containerNetwork = Optional.ofNullable(container.getNetworkSettings())
                .map(ContainerNetworkSettings::getNetworks)
                .orElse(Map.of());

        for (String networkName : networksName) {
            ContainerNetwork net = containerNetwork.get(networkName);

            if (net == null) {
                continue;
            }
            String ip = net.getIpAddress();
            if (ip != null && !ip.isBlank()) {
                return Map.entry(networkName, ip);
            }
        }

        throw new IllegalStateException(
                "At the moment, we weren’t able to retrieve a valid ip address for service '%s'".formatted(serviceId));
    }

    private Map.Entry<String, Integer> getPortInfo(String serviceId, Container container) {

        for (ContainerPort p : container.getPorts()) {
            if (p.getPrivatePort() != null
                    && p.getType() != null
                    && !p.getType().isBlank()) {
                return Map.entry(p.getType(), p.getPrivatePort());
            }
        }

        throw new IllegalStateException(
                "At the moment, we weren’t able to retrieve a valid private port for service '%s'"
                        .formatted(serviceId));
    }

    // Docker stores the container creation time in Unix timestamp format, e.g., (1765261026)
    private String getCreatedAt(Container container) {
        return Instant.ofEpochSecond(container.getCreated()).toString();
    }
}
