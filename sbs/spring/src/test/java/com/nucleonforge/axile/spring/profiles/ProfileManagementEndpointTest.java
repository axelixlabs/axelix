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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(containsBean(updatedBeans, premiumService));
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
        assertTrue(containsBean(updatedBeans, basicService));
        assertTrue(containsBean(updatedBeans, premiumService));

        // Replace profiles
        activateProfiles("profile-legacy,profile-advanced");

        // Verify beans after replacing profiles
        updatedBeans = restTemplate.getForObject("/actuator/beans", Map.class);
        assertTrue(containsBean(updatedBeans, legacyService));
        assertTrue(containsBean(updatedBeans, advancedService));
        assertFalse(containsBean(updatedBeans, basicService));
        assertFalse(containsBean(updatedBeans, premiumService));
    }

    @Test
    void shouldActivateProfiles_AndThenDeactivateAllProfiles() throws InterruptedException {
        String advancedService = "advancedFeatureService", legacyService = "legacyFeatureService";

        checkNoActiveProfilesAndNoBeans(advancedService, legacyService);

        activateProfiles("profile-advanced,profile-legacy");

        // Verify beans after activating profiles
        Map<?, ?> updatedBeans = restTemplate.getForObject("/actuator/beans", Map.class);
        assertTrue(containsBean(updatedBeans, advancedService));
        assertTrue(containsBean(updatedBeans, legacyService));

        // Disable all profiles
        activateProfiles(" ");

        // Verify beans after disabling all profiles
        updatedBeans = restTemplate.getForObject("/actuator/beans", Map.class);
        assertFalse(containsBean(updatedBeans, legacyService));
        assertFalse(containsBean(updatedBeans, advancedService));
    }

    @Test
    void replaceProfiles_shouldReturnBadRequest() {
        ResponseEntity<ProfileMutationResponse> response =
                restTemplate.postForEntity(path(""), defaultEntity(), ProfileMutationResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().updated());

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
        assertEquals(expectedProfiles.length, activeProfiles.size());

        for (String profile : expectedProfiles) {
            assertTrue(activeProfiles.contains(profile));
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
        assertEquals(0, activeProfiles.size());

        Map<?, ?> beans = restTemplate.getForObject("/actuator/beans", Map.class);
        for (String beanName : expectedMissingBeans) {
            assertFalse(containsBean(beans, beanName));
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
