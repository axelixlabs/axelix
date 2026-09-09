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
package com.axelixlabs.axelix.master;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test proving that Axelix Master, when pointed at a Spring Cloud Config Server through the
 * {@code axelix.master.external-config.spring-cloud-config.*} properties, actually fetches remote configuration
 * during start-up and merges it into its {@link Environment}.
 * <p>
 * There is no dedicated and well-supported Spring Cloud Config Server Testcontainer, so a plain
 * {@link GenericContainer} runs the {@code hyness/spring-cloud-config-server} image in its {@code native} profile,
 * serving the properties mounted at {@code /config} (see {@code src/test/resources/config-server/master.properties}).
 * "Native" profile is specific to that pulled image, see the
 * <a href="https://hub.docker.com/r/hyness/spring-cloud-config-server/tags">Description in here</a>
 * <p>
 * The connection settings are contributed as <em>system properties</em> from the static initializer, before the
 * Spring context boots. This is deliberate: {@code spring.config.import: "configserver:"} is resolved during
 * environment post-processing - earlier than {@code @DynamicPropertySource} or {@code @SpringBootTest} inlined
 * properties are made available - so those mechanisms are too late to point Master at the (dynamically mapped)
 * Config Server port.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
class SpringCloudConfigServerIntegrationTest {

    static final String CONFIG_PREFIX = "axelix.master.external-config.spring-cloud-config";

    private static final String SERVED_PROPERTY = "axelix.integration-test.config-server-marker";

    private static final int CONFIG_SERVER_PORT = 8888;

    private static final GenericContainer<?> CONFIG_SERVER = new GenericContainer<>(
                    DockerImageName.parse("hyness/spring-cloud-config-server:5.0.4"))
            .withEnv("SPRING_PROFILES_ACTIVE", "native")
            .withEnv("SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS", "file:/config")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("config-server/master.properties"), "/config/master.properties")
            .withExposedPorts(CONFIG_SERVER_PORT)
            // app is named "master" and the profile for the configuration is the "default" profile
            .waitingFor(
                    Wait.forHttp("/master/default").forPort(CONFIG_SERVER_PORT).forStatusCode(200));

    static {
        CONFIG_SERVER.start();
        System.setProperty(CONFIG_PREFIX + ".enabled", "true");
        System.setProperty(CONFIG_PREFIX + ".name", "master");
        System.setProperty(
                CONFIG_PREFIX + ".uri",
                "http://" + CONFIG_SERVER.getHost() + ":" + CONFIG_SERVER.getMappedPort(CONFIG_SERVER_PORT));
    }

    @Autowired
    private Environment environment;

    @AfterAll
    static void tearDown() {
        System.clearProperty(CONFIG_PREFIX + ".enabled");
        System.clearProperty(CONFIG_PREFIX + ".name");
        System.clearProperty(CONFIG_PREFIX + ".uri");
        CONFIG_SERVER.stop();
    }

    @Test // GH-1579
    void shouldFetchConfigurationFromSpringCloudConfigServer() {
        assertThat(environment.getProperty(SERVED_PROPERTY)).isEqualTo("served-by-config-server");
    }
}
