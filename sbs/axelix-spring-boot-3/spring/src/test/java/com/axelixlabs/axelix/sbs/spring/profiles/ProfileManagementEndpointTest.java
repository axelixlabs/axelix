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
package com.axelixlabs.axelix.sbs.spring.profiles;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.axelixlabs.axelix.sbs.Main;
import com.axelixlabs.axelix.sbs.spring.context.DefaultContextRestarter;
import com.axelixlabs.axelix.sbs.spring.utils.ContextKeepAliveTestListener;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ProfileManagementEndpoint} using {@link TestRestTemplate}
 * and a real HTTP context with a web environment.
 *
 * <p>These tests verify that the actuator endpoint {@code /actuator/profile-management}
 * correctly handles replacement of active Spring profiles at runtime.</p>
 *
 * <p>To be discoverable and enabled during tests, the actuator endpoint must be:</p>
 * <ul>
 *     <li>Explicitly included via {@code management.endpoints.web.exposure.include=profile-management}, or</li>
 *     <li>Configured via auto-configuration in the test application context.</li>
 * </ul>
 *
 * @since 11.07.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(
        listeners = {
            DependencyInjectionTestExecutionListener.class,
            DirtiesContextTestExecutionListener.class,
            ContextKeepAliveTestListener.class
        })
@Import({
    TestFeatureServiceConfigs.PremiumFeatureService.class,
    TestFeatureServiceConfigs.PremiumFeatureServiceConfig.class,
    ProfileManagementEndpoint.class,
    ContextReloadingProfileMutator.class,
    DefaultContextRestarter.class
})
@Disabled("flaky") // TODO: for some reason this test seems to occasionally fail (maybe all of the time)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProfileManagementEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldSwitchFrom_NoActiveProfile_ToPremiumProfile() throws InterruptedException {
        String premiumService = "premiumFeatureService";

        checkNoActiveProfilesAndNoBeans(premiumService);

        activateProfiles(new ProfileMutationRequest(List.of("profile-premium")));

        // Verify bean after activating profile
        Map<?, ?> updatedBeans = restTemplate.getForObject("/actuator/axelix-beans", Map.class);
        assertThat(containsBean(updatedBeans, premiumService)).isTrue();
    }

    @Test
    void shouldReplace_ActiveProfilesDynamically() throws InterruptedException {
        String basicService = "basicFeatureService",
                premiumService = "premiumFeatureService",
                advancedService = "advancedFeatureService",
                legacyService = "legacyFeatureService";

        checkNoActiveProfilesAndNoBeans(basicService, premiumService);

        activateProfiles(new ProfileMutationRequest(List.of("profile-premium", "profile-basic")));

        // Verify beans after activating profiles
        Map<?, ?> updatedBeans = restTemplate.getForObject("/actuator/axelix-beans", Map.class);
        assertThat(containsBean(updatedBeans, basicService)).isTrue();
        assertThat(containsBean(updatedBeans, premiumService)).isTrue();

        // Replace profiles
        activateProfiles(new ProfileMutationRequest(List.of("profile-advanced", "profile-legacy")));

        // Verify beans after replacing profiles
        updatedBeans = restTemplate.getForObject("/actuator/axelix-beans", Map.class);
        assertThat(containsBean(updatedBeans, legacyService)).isTrue();
        assertThat(containsBean(updatedBeans, advancedService)).isTrue();
        assertThat(containsBean(updatedBeans, basicService)).isFalse();
        assertThat(containsBean(updatedBeans, premiumService)).isFalse();
    }

    @Test
    void shouldActivateProfiles_AndThenDeactivateAllProfiles() throws InterruptedException {
        String advancedService = "advancedFeatureService", legacyService = "legacyFeatureService";

        checkNoActiveProfilesAndNoBeans(advancedService, legacyService);

        activateProfiles(new ProfileMutationRequest(List.of("profile-advanced", "profile-legacy")));

        // Verify beans after activating profiles
        Map<?, ?> updatedBeans = restTemplate.getForObject("/actuator/axelix-beans", Map.class);
        assertThat(containsBean(updatedBeans, advancedService)).isTrue();
        assertThat(containsBean(updatedBeans, legacyService)).isTrue();

        // Disable all profiles
        activateProfiles(new ProfileMutationRequest(Collections.emptyList()));

        // Verify beans after disabling all profiles
        updatedBeans = restTemplate.getForObject("/actuator/axelix-beans", Map.class);
        assertThat(containsBean(updatedBeans, advancedService)).isFalse();
        assertThat(containsBean(updatedBeans, legacyService)).isFalse();
    }

    /**
     * Activates the given list of Spring profiles via the custom Actuator endpoint.
     * Verifies the activation and pauses to allow context reload.
     *
     * @param request the request object containing the list of profiles to activate (an empty list will clear all active profiles)
     */
    private void activateProfiles(ProfileMutationRequest request) throws InterruptedException {
        ResponseEntity<ProfileMutationResponse> response =
                restTemplate.postForEntity(path(), defaultEntity(request), ProfileMutationResponse.class);

        TimeUnit.SECONDS.sleep(7); // wait for context update
        assertThat(response)
                .isNotNull()
                .returns(HttpStatus.OK, ResponseEntity::getStatusCode)
                .extracting(ResponseEntity::getBody)
                .isNotNull()
                .returns(true, ProfileMutationResponse::updated);

        checkActiveProfiles(request);
    }

    /**
     * Checks that the given profiles are currently active by calling the Actuator /env endpoint.
     *
     * @param request contains the expected list of active profiles
     */
    @SuppressWarnings("unchecked")
    private void checkActiveProfiles(ProfileMutationRequest request) {
        List<String> expectedProfiles = request.effectiveProfiles();

        Map<?, ?> env = restTemplate.getForObject("/actuator/env", Map.class);
        List<String> activeProfiles = (List<String>) env.get("activeProfiles");
        assertThat(activeProfiles).hasSize(expectedProfiles.size());

        for (String profile : expectedProfiles) {
            assertThat(activeProfiles).contains(profile);
        }
    }

    /**
     * Verifies that no profiles are currently active and that the given beans are not present in the context.
     *
     * @param expectedMissingBeans list of bean names that should not be registered
     */
    @SuppressWarnings("unchecked")
    private void checkNoActiveProfilesAndNoBeans(String... expectedMissingBeans) {
        Map<?, ?> env = restTemplate.getForObject("/actuator/env", Map.class);
        List<String> activeProfiles = (List<String>) env.get("activeProfiles");
        assertThat(activeProfiles).isEmpty();

        Map<?, ?> beans = restTemplate.getForObject("/actuator/axelix-beans", Map.class);
        for (String beanName : expectedMissingBeans) {
            assertThat(containsBean(beans, beanName)).isFalse();
        }
    }

    @SuppressWarnings("unchecked")
    private boolean containsBean(Map<?, ?> beansResponse, String expectedBeanName) {
        Map<String, Object> contexts = (Map<String, Object>) beansResponse.get("contexts");
        for (Object contextObj : contexts.values()) {
            Map<String, Object> context = (Map<String, Object>) contextObj;
            Map<String, Object> beans = (Map<String, Object>) context.get("beans");
            if (beans != null && beans.containsKey(expectedBeanName)) {
                return true;
            }
        }
        return false;
    }

    private HttpEntity<ProfileMutationRequest> defaultEntity(ProfileMutationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }

    private String path() {
        return "/actuator/axelix-profile-management";
    }
}
