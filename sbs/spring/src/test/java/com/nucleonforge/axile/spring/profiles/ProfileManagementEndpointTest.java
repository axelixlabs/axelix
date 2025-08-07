package com.nucleonforge.axile.spring.profiles;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

import com.nucleonforge.axile.Main;
import com.nucleonforge.axile.spring.utils.ContextKeepAliveTestListener;

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
    TestFeatureServiceConfigs.PremiumFeatureServiceConfig.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProfileManagementEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldSwitchFrom_NoActiveProfile_ToPremiumProfile() throws InterruptedException {
        String premiumService = "premiumFeatureService";

        checkNoActiveProfilesAndNoBeans(premiumService);

        activateProfiles("profile-premium");

        // Verify bean after activating profile
        Map<?, ?> updatedBeans = restTemplate.getForObject("/actuator/beans", Map.class);
        assertThat(containsBean(updatedBeans, premiumService)).isTrue();
    }

    @Test
    void shouldReplace_ActiveProfilesDynamically() throws InterruptedException {
        String basicService = "basicFeatureService",
            premiumService = "premiumFeatureService",
            advancedService = "advancedFeatureService",
            legacyService = "legacyFeatureService";

        checkNoActiveProfilesAndNoBeans(basicService, premiumService);

        activateProfiles("profile-premium,profile-basic");

        // Verify beans after activating profiles
        Map<?, ?> updatedBeans = restTemplate.getForObject("/actuator/beans", Map.class);
        assertThat(containsBean(updatedBeans, basicService)).isTrue();
        assertThat(containsBean(updatedBeans, premiumService)).isTrue();

        // Replace profiles
        activateProfiles("profile-legacy,profile-advanced");

        // Verify beans after replacing profiles
        updatedBeans = restTemplate.getForObject("/actuator/beans", Map.class);
        assertThat(containsBean(updatedBeans, legacyService)).isTrue();
        assertThat(containsBean(updatedBeans, advancedService)).isTrue();
        assertThat(containsBean(updatedBeans, basicService)).isFalse();
        assertThat(containsBean(updatedBeans, premiumService)).isFalse();
    }

    @Test
    void shouldActivateProfiles_AndThenDeactivateAllProfiles() throws InterruptedException {
        String advancedService = "advancedFeatureService", legacyService = "legacyFeatureService";

        checkNoActiveProfilesAndNoBeans(advancedService, legacyService);

        activateProfiles("profile-advanced,profile-legacy");

        // Verify beans after activating profiles
        Map<?, ?> updatedBeans = restTemplate.getForObject("/actuator/beans", Map.class);
        assertThat(containsBean(updatedBeans, advancedService)).isTrue();
        assertThat(containsBean(updatedBeans, legacyService)).isTrue();

        // Disable all profiles
        activateProfiles(" ");

        // Verify beans after disabling all profiles
        updatedBeans = restTemplate.getForObject("/actuator/beans", Map.class);
        assertThat(containsBean(updatedBeans, advancedService)).isFalse();
        assertThat(containsBean(updatedBeans, legacyService)).isFalse();
    }

    @Test
    void replaceProfiles_shouldReturnBadRequest() {
        ResponseEntity<ProfileMutationResponse> response =
            restTemplate.postForEntity(path(""), defaultEntity(), ProfileMutationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * Activates the given comma-separated list of Spring profiles via the custom Actuator endpoint.
     * Verifies the activation and pauses to allow context reload.
     *
     * @param profiles comma-separated string of profiles to activate (use blank to clear all profiles)
     */
    private void activateProfiles(String profiles) throws InterruptedException {
        ResponseEntity<ProfileMutationResponse> response =
            restTemplate.postForEntity(path("/" + profiles), defaultEntity(), ProfileMutationResponse.class);

        TimeUnit.SECONDS.sleep(7); // wait for context update
        assertThat(response)
            .isNotNull()
            .returns(HttpStatus.OK, ResponseEntity::getStatusCode)
            .extracting(ResponseEntity::getBody)
            .isNotNull()
            .returns(true, ProfileMutationResponse::updated);

        String[] expectedProfiles = profiles.isBlank() ? new String[0] : profiles.split(",");
        checkActiveProfiles(expectedProfiles);
    }

    /**
     * Checks that the given profiles are currently active by calling the Actuator /env endpoint.
     *
     * @param expectedProfiles the expected list of active profiles
     */
    @SuppressWarnings("unchecked")
    private void checkActiveProfiles(String... expectedProfiles) {
        Map<?, ?> env = restTemplate.getForObject("/actuator/env", Map.class);
        List<String> activeProfiles = (List<String>) env.get("activeProfiles");
        assertThat(activeProfiles).hasSize(expectedProfiles.length);

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

        Map<?, ?> beans = restTemplate.getForObject("/actuator/beans", Map.class);
        for (String beanName : expectedMissingBeans) {
            assertThat(containsBean(beans, beanName)).isFalse();
        }
    }

    /**
     * Helper to check if a given bean exists in the /actuator/beans response.
     */
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

    /**
     * Helper to creates a default HttpEntity with application/json headers and no body.
     */
    private HttpEntity<Void> defaultEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(headers);
    }

    /**
     * Helper to construct a relative path to the profile-management actuator endpoint.
     */
    private String path(String relative) {
        return "/actuator/profile-management" + relative;
    }
}
