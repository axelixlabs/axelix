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
package com.axelixlabs.axelix.sbs.spring.integrations.http;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.sbs.Main;

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
