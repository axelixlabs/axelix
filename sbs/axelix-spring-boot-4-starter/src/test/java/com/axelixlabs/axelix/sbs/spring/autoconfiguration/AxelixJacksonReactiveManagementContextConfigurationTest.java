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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.sbs.spring.core.IgnoreTestContextArchitecture;
import com.axelixlabs.axelix.sbs.spring.core.Main;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixJacksonManagementContextConfiguration} on WebFlux.
 *
 * @author Darshan A S
 */
@IgnoreTestContextArchitecture(reason = "Requires custom ObjectMapper to verify Jackson wire format isolation")
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "spring.main.web-application-type=reactive",
            "spring.autoconfigure.exclude="
                    + "org.springframework.boot.tomcat.autoconfigure.actuate.web.server.TomcatReactiveManagementContextAutoConfiguration",
            "management.endpoints.web.exposure.include="
                    + AxelixJacksonReactiveManagementContextConfigurationTest.ENDPOINT_ID
        })
@AutoConfigureTestRestTemplate
@Import(AxelixJacksonReactiveManagementContextConfigurationTest.CustomJacksonConfig.class)
class AxelixJacksonReactiveManagementContextConfigurationTest {

    static final String ENDPOINT_ID = "jacksonisolationreactive";

    @TestConfiguration
    static class CustomJacksonConfig {

        @Bean
        @Primary
        public JsonMapper customMapper() {
            return JsonMapper.builder()
                    .disable(MapperFeature.USE_ANNOTATIONS)
                    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    .build();
        }

        @Bean
        public JacksonIsolationEndpoint jacksonIsolationEndpoint() {
            return new JacksonIsolationEndpoint();
        }
    }

    @Endpoint(id = ENDPOINT_ID)
    static class JacksonIsolationEndpoint {

        @ReadOperation
        public Payload read() {
            return new Payload("value");
        }
    }

    public static class Payload {

        private final String propertyName;

        Payload(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test // GH-1842
    void shouldKeepCamelCaseWireFormatEvenWhenJacksonConfiguredWithSnakeCaseOnWebFlux() {
        // when.
        ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/actuator/" + ENDPOINT_ID, String.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        JsonAssertions.assertThatJson(response.getBody())
                .isObject()
                .containsKey("propertyName")
                .doesNotContainKey("property_name");
    }

    @Test
    void shouldNotRegisterWebFluxConfigurerWhenAxelixStarterIsDisabled() {
        new ReactiveWebApplicationContextRunner()
                .withPropertyValues("axelix.sbs.enabled=false")
                .withUserConfiguration(AxelixJacksonManagementContextConfiguration.class)
                .run(context -> assertThat(context).doesNotHaveBean("axelixEndpointWebFluxConfigurer"));
    }
}
