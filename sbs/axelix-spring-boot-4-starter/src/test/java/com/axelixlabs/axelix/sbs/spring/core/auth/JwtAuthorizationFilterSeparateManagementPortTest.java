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
package com.axelixlabs.axelix.sbs.spring.core.auth;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.testfixtures.TestRoles;
import com.axelixlabs.axelix.common.testfixtures.UserUtils;
import com.axelixlabs.axelix.common.utils.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.JwtAuthAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.IgnoreTestContextArchitecture;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.core.env.AxelixEnvironmentEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvironmentService;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvironmentTestConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link JwtAuthorizationFilter} when Actuator runs on a separate management port.
 *
 * @author Nikita Kirillov
 * @since 19.10.2026
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "management.server.port=0")
@AutoConfigureTestRestTemplate
@Import({
    JwtAuthAutoConfiguration.class,
    EnvironmentTestConfig.class,
    JwtAuthorizationFilterSeparateManagementPortTest.EnvEndpointConfiguration.class
})
@IgnoreTestContextArchitecture(reason = "Requires a separate management port, hence a dedicated context")
class JwtAuthorizationFilterSeparateManagementPortTest {

    @LocalManagementPort
    private int managementPort;

    @LocalServerPort
    private int serverPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtEncoderService jwtEncoderService;

    @Test
    void shouldReturnUnauthorized_TokenIsMissing() {
        ResponseEntity<String> response = restTemplate.getForEntity(envEndpointUrl(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAllowAccess_TokenIsValid() {
        String token = jwtEncoderService.generateToken(
                UserUtils.withPassword("testUser", "testPassword", Set.of(TestRoles.ADMIN)));
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, AuthenticationSchemes.BEARER.prefix() + token);

        ResponseEntity<String> response =
                restTemplate.exchange(envEndpointUrl(), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldNotRegisterFilter_OnApplicationPort() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + serverPort + "/actuator/axelix-env", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String envEndpointUrl() {
        return "http://localhost:" + managementPort + "/actuator/axelix-env";
    }

    @TestConfiguration
    static class EnvEndpointConfiguration {

        @Bean
        public AxelixEnvironmentEndpoint axelixEnvironmentEndpoint(
                EnvironmentService environmentService, SecurityContextExecutor securityContextExecutor) {
            return new AxelixEnvironmentEndpoint(environmentService, securityContextExecutor);
        }

        @Bean
        public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
            return new SmartSanitizingFunction(List.of(), propertyNameNormalizer);
        }
    }
}
