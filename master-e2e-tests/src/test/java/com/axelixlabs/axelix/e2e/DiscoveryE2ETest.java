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
package com.axelixlabs.axelix.e2e;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.e2e.client.AxelixMasterApiClient;
import com.axelixlabs.axelix.e2e.config.E2ETestConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end smoke tests for validating axelix-master discovery modes and instance registration.
 *
 * @author Nikita Kirillov
 */
public class DiscoveryE2ETest {

    private static final String AUTO_DISCOVERY_INSTANCE_NAME = "notification-service-automatic-discovery";
    private static final String SELF_REG_INSTANCE_NAME = "notification-service-self-registration";

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(3);
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);

    private final Set<String> expectedInstanceNames = new HashSet<>();

    private AxelixMasterApiClient client;

    @BeforeEach
    void setUp() {
        expectedInstanceNames.clear();

        if (E2ETestConfig.isAutoDiscoveryMode()) {
            expectedInstanceNames.add(AUTO_DISCOVERY_INSTANCE_NAME);
        }
        if (E2ETestConfig.isDiscoveryModeSelfReg()) {
            expectedInstanceNames.add(SELF_REG_INSTANCE_NAME);
        }

        client = new AxelixMasterApiClient(E2ETestConfig.masterBaseUrl());
        client.login(E2ETestConfig.superAdminUsername(), E2ETestConfig.superAdminPassword());
    }

    @Test
    void instancesAreSuccessfullyDiscoveredAndRegisteredInMaster() {
        if (expectedInstanceNames.isEmpty()) {
            throw new AssertionError("""
                    Test configuration error: The set of expected instance names is empty.
                    Check if discovery flags (Auto/SelfReg) are properly passed from PICT matrix into the test context.
                    """);
        }

        try {
            Awaitility.await("waiting for " + expectedInstanceNames + " to self-register/be-discovered in Master")
                    .atMost(DEFAULT_TIMEOUT)
                    .pollInterval(DEFAULT_POLL_INTERVAL)
                    .pollDelay(Duration.ofSeconds(2))
                    .untilAsserted(() -> {
                        Set<String> registeredInstanceNames = client.getRegisteredInstanceNames();

                        // K8s appends random suffixes to pod names; match by prefix only.
                        for (String expectedInstanceName : expectedInstanceNames) {
                            Optional<String> foundInstance = registeredInstanceNames.stream()
                                    .filter(instanceName -> instanceName.startsWith(expectedInstanceName))
                                    .findFirst();

                            assertThat(foundInstance).isPresent();
                        }
                    });
        } catch (ConditionTimeoutException e) {
            Set<String> lastKnown = client.getRegisteredInstanceNames();
            throw new AssertionError(
                    "Timed out after %s waiting for instances %s to register. Currently registered: %s"
                            .formatted(DEFAULT_TIMEOUT, expectedInstanceNames, lastKnown),
                    e);
        }
    }
}
