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
package com.axelixlabs.axelix.sbs.spring.core.integrations.feign;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.common.api.integrations.feign.FeignIntegration;
import com.axelixlabs.axelix.common.domain.http.HttpVersion;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixFeignEndpoint}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.endpoints.web.exposure.include=axelix-feign")
@Import(AxelixFeignEndpointTest.AxelixFeignEndpointTestConfiguration.class)
public class AxelixFeignEndpointTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @ParameterizedTest
    @MethodSource("feignClientArgs")
    void shouldFeignClientFeed(
            String serviceName, List<String> networkAddresses, List<FeignIntegration.FeignHttpMethod> httpMethods) {

        ResponseEntity<Set<FeignIntegration>> response = testRestTemplate.exchange(
                "/actuator/axelix-feign",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Set<FeignIntegration>>() {});

        FeignIntegration serviceIntegration = response.getBody().stream()
                .filter(integration -> serviceName.equals(integration.getServiceName()))
                .findFirst()
                .orElseThrow();

        assertThat(serviceIntegration)
                .returns(serviceName, FeignIntegration::getServiceName)
                .returns(networkAddresses, FeignIntegration::getNetworkAddresses)
                .returns(HttpVersion.V1_1.getDisplay(), FeignIntegration::getProtocol)
                .satisfies(integration ->
                        assertThat(integration.getHttpMethods()).containsExactlyInAnyOrderElementsOf(httpMethods));
    }

    public static Stream<Arguments> feignClientArgs() {
        List<FeignIntegration.FeignHttpMethod> httpMethods = List.of(
                new FeignIntegration.FeignHttpMethod("POST", "/post"),
                new FeignIntegration.FeignHttpMethod("GET", "/get"),
                new FeignIntegration.FeignHttpMethod("PUT", "/put"),
                new FeignIntegration.FeignHttpMethod("DELETE", "/delete"),
                new FeignIntegration.FeignHttpMethod("UNKNOWN", "/request"));
        return Stream.of(
                Arguments.of("Service-1", List.of("http://service1-api"), httpMethods),
                Arguments.of("Service-2", List.of(), httpMethods),
                Arguments.of(
                        "ServiceDiscovery", List.of("http://localhost:8081", "http://localhost:8082"), httpMethods));
    }

    @TestConfiguration
    static class AxelixFeignEndpointTestConfiguration {

        @Bean
        public AxelixFeignEndpoint axelixFeignEndpoint(FeignClientIntegrationDiscoverer discoverer) {
            return new AxelixFeignEndpoint(discoverer);
        }

        @Bean
        public FeignClientIntegrationDiscoverer feignClientIntegrationDiscoverer(
                ApplicationContext applicationContext, DiscoveryClient discoveryClient) {
            return new FeignClientIntegrationDiscoverer(applicationContext, discoveryClient);
        }

        @Bean
        DiscoveryClient discoveryClient() {
            return new DiscoveryClient() {

                @Override
                public String description() {
                    return "test-discovery-client";
                }

                @Override
                public List<ServiceInstance> getInstances(String serviceId) {
                    if ("ServiceDiscovery".equals(serviceId)) {
                        return List.of(
                                new TestServiceInstance(serviceId, URI.create("http://localhost:8081")),
                                new TestServiceInstance(serviceId, URI.create("http://localhost:8082")));
                    }
                    return List.of();
                }

                @Override
                public List<String> getServices() {
                    return List.of("ServiceDiscovery");
                }
            };
        }
    }

    @FeignClient(contextId = "context-1", name = "Service-1", url = "http://service1-api")
    interface TestFeignClient1 {
        @PostMapping("/post")
        void post();

        @GetMapping("/get")
        void get();

        @PutMapping("/put")
        void put();

        @DeleteMapping("/delete")
        void delete();

        @RequestMapping("/request")
        void request();
    }

    @FeignClient(contextId = "context-2", name = "Service-2")
    interface TestFeignClient2 {
        @PostMapping("/post")
        void post();

        @GetMapping("/get")
        void get();

        @PutMapping("/put")
        void put();

        @DeleteMapping("/delete")
        void delete();

        @RequestMapping("/request")
        void request();
    }

    @FeignClient(contextId = "context-discovery", name = "ServiceDiscovery")
    interface TestFeignClientDiscovery {
        @PostMapping("/post")
        void post();

        @GetMapping("/get")
        void get();

        @PutMapping("/put")
        void put();

        @DeleteMapping("/delete")
        void delete();

        @RequestMapping("/request")
        void request();
    }

    static final class TestServiceInstance implements ServiceInstance {

        private final String serviceId;
        private final URI uri;

        private TestServiceInstance(String serviceId, URI uri) {
            this.serviceId = serviceId;
            this.uri = uri;
        }

        @Override
        public String getServiceId() {
            return serviceId;
        }

        @Override
        public String getHost() {
            return uri.getHost();
        }

        @Override
        public int getPort() {
            return uri.getPort();
        }

        @Override
        public boolean isSecure() {
            return "https".equalsIgnoreCase(uri.getScheme());
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public Map<String, String> getMetadata() {
            return Map.of();
        }
    }
}
