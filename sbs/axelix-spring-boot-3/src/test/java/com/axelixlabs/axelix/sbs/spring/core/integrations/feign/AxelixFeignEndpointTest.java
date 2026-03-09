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

import org.junit.jupiter.api.Test;

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

import com.axelixlabs.axelix.common.api.integration.FeignIntegration;
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

    private static final String SERVICE_WITH_PATH_IN_FEIGN_ANNOTATION = "service-1";
    private static final String SERVICE_WITH_PATH_IN_FEIGN_ANNOTATION_AND_PATH_WITHOUT_MAPPING_ANNOTATION = "service-2";
    private static final String SERVICE_WITH_WITHOUT_IN_FEIGN_ANNOTATION_AND_PATH_WITHOUT_MAPPING_ANNOTATION =
            "service-3";
    private static final String SERVICE_WITHOUT_URL = "service-4";
    private static final String SERVICE_DISCOVERY = "service-5";

    private static final String NETWORK_ADDRESS_1 = "http://service1-api";
    private static final String NETWORK_ADDRESS_2 = "http://service1-api";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldReturnService_WithPathInFeignAnnotation() {
        List<FeignIntegration.FeignHttpMethod> httpMethods = List.of(
                new FeignIntegration.FeignHttpMethod("POST", "/path/post"),
                new FeignIntegration.FeignHttpMethod("GET", "/path/get"),
                new FeignIntegration.FeignHttpMethod("PUT", "/path/put"),
                new FeignIntegration.FeignHttpMethod("DELETE", "/path/delete"),
                new FeignIntegration.FeignHttpMethod("UNKNOWN", "/path/request"));

        FeignIntegration service = getFeignIntegration(SERVICE_WITH_PATH_IN_FEIGN_ANNOTATION);

        assertThat(service)
                .returns(SERVICE_WITH_PATH_IN_FEIGN_ANNOTATION, FeignIntegration::getServiceName)
                .returns(List.of(NETWORK_ADDRESS_1), FeignIntegration::getNetworkAddresses)
                .returns(HttpVersion.V1_1.getDisplay(), FeignIntegration::getProtocol)
                .satisfies(integration ->
                        assertThat(integration.getHttpMethods()).containsExactlyInAnyOrderElementsOf(httpMethods));
    }

    @Test
    void shouldReturnService_WithPathInFeignAnnotationAndPathWithoutMappingAnnotation() {
        List<FeignIntegration.FeignHttpMethod> httpMethods = List.of(
                new FeignIntegration.FeignHttpMethod("POST", "/path"),
                new FeignIntegration.FeignHttpMethod("GET", "/path"),
                new FeignIntegration.FeignHttpMethod("PUT", "/path"),
                new FeignIntegration.FeignHttpMethod("DELETE", "/path"),
                new FeignIntegration.FeignHttpMethod("UNKNOWN", "/path"));

        FeignIntegration service =
                getFeignIntegration(SERVICE_WITH_PATH_IN_FEIGN_ANNOTATION_AND_PATH_WITHOUT_MAPPING_ANNOTATION);

        assertThat(service)
                .returns(
                        SERVICE_WITH_PATH_IN_FEIGN_ANNOTATION_AND_PATH_WITHOUT_MAPPING_ANNOTATION,
                        FeignIntegration::getServiceName)
                .returns(List.of(NETWORK_ADDRESS_1), FeignIntegration::getNetworkAddresses)
                .returns(HttpVersion.V1_1.getDisplay(), FeignIntegration::getProtocol)
                .satisfies(integration ->
                        assertThat(integration.getHttpMethods()).containsExactlyInAnyOrderElementsOf(httpMethods));
    }

    @Test
    void shouldReturnService_WithoutPathInFeignAnnotationAndPathWithoutMappingAnnotation() {
        List<FeignIntegration.FeignHttpMethod> httpMethods = List.of(
                new FeignIntegration.FeignHttpMethod("POST", null),
                new FeignIntegration.FeignHttpMethod("GET", null),
                new FeignIntegration.FeignHttpMethod("PUT", null),
                new FeignIntegration.FeignHttpMethod("DELETE", null),
                new FeignIntegration.FeignHttpMethod("UNKNOWN", null));

        FeignIntegration service =
                getFeignIntegration(SERVICE_WITH_WITHOUT_IN_FEIGN_ANNOTATION_AND_PATH_WITHOUT_MAPPING_ANNOTATION);

        assertThat(service)
                .returns(
                        SERVICE_WITH_WITHOUT_IN_FEIGN_ANNOTATION_AND_PATH_WITHOUT_MAPPING_ANNOTATION,
                        FeignIntegration::getServiceName)
                .returns(List.of(NETWORK_ADDRESS_1), FeignIntegration::getNetworkAddresses)
                .returns(HttpVersion.V1_1.getDisplay(), FeignIntegration::getProtocol)
                .satisfies(integration ->
                        assertThat(integration.getHttpMethods()).containsExactlyInAnyOrderElementsOf(httpMethods));
    }

    // This is not a valid scenario, and OpenFeign cannot work with a service whose URL
    // for load balancing is not defined. However, we still handle this case so that the service
    // can appear in the services feed.
    @Test
    void shouldReturnService_WithoutURL() {
        List<FeignIntegration.FeignHttpMethod> httpMethods = List.of(
                new FeignIntegration.FeignHttpMethod("POST", "/post"),
                new FeignIntegration.FeignHttpMethod("GET", "/get"),
                new FeignIntegration.FeignHttpMethod("PUT", "/put"),
                new FeignIntegration.FeignHttpMethod("DELETE", "/delete"),
                new FeignIntegration.FeignHttpMethod("UNKNOWN", "/request"));

        FeignIntegration service = getFeignIntegration(SERVICE_WITHOUT_URL);

        assertThat(service)
                .returns(SERVICE_WITHOUT_URL, FeignIntegration::getServiceName)
                .returns(List.of(), FeignIntegration::getNetworkAddresses)
                .returns(HttpVersion.V1_1.getDisplay(), FeignIntegration::getProtocol)
                .satisfies(integration ->
                        assertThat(integration.getHttpMethods()).containsExactlyInAnyOrderElementsOf(httpMethods));
    }

    @Test
    void shouldReturnService_Discovery() {
        List<FeignIntegration.FeignHttpMethod> httpMethods = List.of(
                new FeignIntegration.FeignHttpMethod("POST", "/path/post"),
                new FeignIntegration.FeignHttpMethod("GET", "/path/get"),
                new FeignIntegration.FeignHttpMethod("PUT", "/path/put"),
                new FeignIntegration.FeignHttpMethod("DELETE", "/path/delete"),
                new FeignIntegration.FeignHttpMethod("UNKNOWN", "/path/request"));

        FeignIntegration service = getFeignIntegration(SERVICE_DISCOVERY);

        assertThat(service)
                .returns(SERVICE_DISCOVERY, FeignIntegration::getServiceName)
                .returns(List.of(NETWORK_ADDRESS_1, NETWORK_ADDRESS_2), FeignIntegration::getNetworkAddresses)
                .returns(HttpVersion.V1_1.getDisplay(), FeignIntegration::getProtocol)
                .satisfies(integration ->
                        assertThat(integration.getHttpMethods()).containsExactlyInAnyOrderElementsOf(httpMethods));
    }

    private FeignIntegration getFeignIntegration(String serviceName) {
        ResponseEntity<Set<FeignIntegration>> response = testRestTemplate.exchange(
                "/actuator/axelix-feign",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Set<FeignIntegration>>() {});

        return response.getBody().stream()
                .filter(integration -> serviceName.equals(integration.getServiceName()))
                .findFirst()
                .orElseThrow();
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
                    if (SERVICE_DISCOVERY.equals(serviceId)) {
                        return List.of(
                                new TestServiceInstance(serviceId, URI.create(NETWORK_ADDRESS_1)),
                                new TestServiceInstance(serviceId, URI.create(NETWORK_ADDRESS_2)));
                    }
                    return List.of();
                }

                @Override
                public List<String> getServices() {
                    return List.of(SERVICE_DISCOVERY);
                }
            };
        }
    }

    @FeignClient(
            contextId = "context-1",
            name = SERVICE_WITH_PATH_IN_FEIGN_ANNOTATION,
            url = NETWORK_ADDRESS_1,
            path = "/path")
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

    @FeignClient(
            contextId = "context-2",
            name = SERVICE_WITH_PATH_IN_FEIGN_ANNOTATION_AND_PATH_WITHOUT_MAPPING_ANNOTATION,
            url = NETWORK_ADDRESS_1,
            path = "/path")
    interface TestFeignClient2 {
        @PostMapping()
        void post();

        @GetMapping()
        void get();

        @PutMapping()
        void put();

        @DeleteMapping()
        void delete();

        @RequestMapping()
        void request();
    }

    @FeignClient(
            contextId = "context-3",
            name = SERVICE_WITH_WITHOUT_IN_FEIGN_ANNOTATION_AND_PATH_WITHOUT_MAPPING_ANNOTATION,
            url = NETWORK_ADDRESS_1)
    interface TestFeignClient3 {
        @PostMapping()
        void post();

        @GetMapping()
        void get();

        @PutMapping()
        void put();

        @DeleteMapping()
        void delete();

        @RequestMapping()
        void request();
    }

    @FeignClient(contextId = "context-4", name = SERVICE_WITHOUT_URL)
    interface TestFeignClient4 {
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

    @FeignClient(contextId = "context-5", name = SERVICE_DISCOVERY, path = "/path")
    interface TestFeignClient5 {
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
