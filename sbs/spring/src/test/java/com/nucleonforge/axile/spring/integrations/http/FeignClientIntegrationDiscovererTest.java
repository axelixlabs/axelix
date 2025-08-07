package com.nucleonforge.axile.spring.integrations.http;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignClient;

import com.nucleonforge.axile.Main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link FeignClientIntegrationDiscoverer}, verifying
 * that Feign clients are properly discovered and mapped into {@link HttpIntegration} instances.
 *
 * @since 09.07.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = Main.class)
class FeignClientIntegrationDiscovererTest {

    @Autowired
    private FeignClientIntegrationDiscoverer discoverer;

    @Test
    void discoverIntegrations_shouldCorrectlyProcessTestFeignClient1() {
        Set<HttpIntegration> integrations = discoverer.discoverIntegrations();

        HttpIntegration service2Integration = integrations.stream()
                .filter(integration -> "Service1".equals(integration.entityType()))
                .findFirst()
                .orElseThrow();

        assertEquals("Service1", service2Integration.entityType());
        assertEquals("http://service1-api", service2Integration.networkAddress());
        assertEquals(HttpVersion.V1_1.getDisplay(), service2Integration.protocol());
    }

    @Test
    void discoverIntegrations_shouldCorrectlyProcessTestFeignClient2() {
        Set<HttpIntegration> integrations = discoverer.discoverIntegrations();

        HttpIntegration service2Integration = integrations.stream()
                .filter(integration -> "Service2".equals(integration.entityType()))
                .findFirst()
                .orElseThrow();

        assertEquals("Service2", service2Integration.entityType());
        assertEquals("discovered://Service2", service2Integration.networkAddress());
        assertEquals(HttpVersion.V1_1.getDisplay(), service2Integration.protocol());
    }

    @Test
    void discoverIntegrations_shouldDiscoverAllFeignClients() {
        Set<HttpIntegration> integrations = discoverer.discoverIntegrations();

        assertEquals(2, integrations.size());

        assertTrue(integrations.stream().anyMatch(integration -> "Service1".equals(integration.entityType())));

        assertTrue(integrations.stream().anyMatch(integration -> "Service2".equals(integration.entityType())));
    }

    @FeignClient(name = "Service1", url = "http://service1-api")
    interface TestFeignClient1 {}

    @FeignClient(name = "Service2")
    interface TestFeignClient2 {}
}
