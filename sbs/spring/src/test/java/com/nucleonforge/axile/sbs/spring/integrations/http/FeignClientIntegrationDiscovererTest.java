package com.nucleonforge.axile.sbs.spring.integrations.http;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Import;

import com.nucleonforge.axile.Main;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link FeignClientIntegrationDiscoverer}, verifying
 * that Feign clients are properly discovered and mapped into {@link HttpIntegration} instances.
 *
 * @since 09.07.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = Main.class)
@Import(FeignClientIntegrationDiscoverer.class)
class FeignClientIntegrationDiscovererTest {

    @Autowired
    private FeignClientIntegrationDiscoverer discoverer;

    @Test
    void discoverIntegrations_shouldCorrectlyProcessTestFeignClient1() {
        Set<HttpIntegration> integrations = discoverer.discoverIntegrations();

        HttpIntegration serviceIntegration = integrations.stream()
                .filter(integration -> "Service1".equals(integration.entityType()))
                .findFirst()
                .orElseThrow();

        assertThat(serviceIntegration)
                .returns("Service1", HttpIntegration::entityType)
                .returns("http://service1-api", HttpIntegration::networkAddress)
                .returns(HttpVersion.V1_1.getDisplay(), HttpIntegration::protocol);
    }

    @Test
    void discoverIntegrations_shouldCorrectlyProcessTestFeignClient2() {
        Set<HttpIntegration> integrations = discoverer.discoverIntegrations();

        HttpIntegration serviceIntegration = integrations.stream()
                .filter(integration -> "Service2".equals(integration.entityType()))
                .findFirst()
                .orElseThrow();

        assertThat(serviceIntegration)
                .returns("Service2", HttpIntegration::entityType)
                .returns("discovered://Service2", HttpIntegration::networkAddress)
                .returns(HttpVersion.V1_1.getDisplay(), HttpIntegration::protocol);
    }

    @Test
    void discoverIntegrations_shouldDiscoverAllFeignClients() {
        Set<HttpIntegration> integrations = discoverer.discoverIntegrations();

        assertThat(integrations)
                .hasSize(2)
                .extracting(HttpIntegration::entityType)
                .containsExactlyInAnyOrder("Service1", "Service2");
    }

    @FeignClient(name = "Service1", url = "http://service1-api")
    interface TestFeignClient1 {}

    @FeignClient(name = "Service2")
    interface TestFeignClient2 {}
}
