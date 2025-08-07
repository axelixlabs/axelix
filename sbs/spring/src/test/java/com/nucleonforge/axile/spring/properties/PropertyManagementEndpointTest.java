package com.nucleonforge.axile.spring.properties;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.nucleonforge.axile.spring.utils.ContextKeepAliveTestListener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link PropertyManagementEndpoint} using {@link TestRestTemplate}
 * and a real HTTP context with web environment.
 *
 * <p>These tests verify that the actuator endpoint {@code /actuator/property-management}
 * correctly handles property mutation operations, including validation of input parameters
 * and successful updates of property values.</p>
 *
 * <p>To be discoverable and enabled during tests, the actuator endpoint should either be:
 * <ul>
 *     <li>Explicitly included via {@code management.endpoints.web.exposure.include=property-management}, or</li>
 *     <li>Configured as part of auto-configuration in the test application context.</li>
 * </ul>
 *
 * @since 10.07.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(
        listeners = {
            DependencyInjectionTestExecutionListener.class,
            DirtiesContextTestExecutionListener.class,
            ContextKeepAliveTestListener.class
        })
@TestPropertySource(
        properties = {"myEmpty.property= ", "notEmpty.property=not-empty", "management.endpoint.env.show-values=always"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PropertyManagementEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void mutate_shouldUpdatePropertyValue() throws InterruptedException {
        Map<?, ?> initialResponse = restTemplate.getForObject("/actuator/env/myEmpty.property", Map.class);
        assertNotNull(initialResponse);
        Map<?, ?> property = (Map<?, ?>) initialResponse.get("property");
        assertNotNull(property);
        String initialValue = (String) property.get("value");
        assertTrue(initialValue.isEmpty());

        String newValue = "new-value";

        mutateProperty("myEmpty.property", newValue);

        Map<?, ?> updatedResponse = restTemplate.getForObject("/actuator/env/myEmpty.property", Map.class);
        assertNotNull(updatedResponse);
        Map<?, ?> updatedProperty = (Map<?, ?>) updatedResponse.get("property");
        assertNotNull(updatedProperty);
        String actualValue = (String) updatedProperty.get("value");
        assertEquals(newValue, actualValue);
    }

    @Test
    void mutate_shouldNotMutate_whenPropertyNameIsBlank() {
        ResponseEntity<MutationResponse> blankNameResponse =
                restTemplate.postForEntity(path("/ \t?newValue=value"), defaultEntity(), MutationResponse.class);
        assertEquals(HttpStatus.OK, blankNameResponse.getStatusCode());
        MutationResponse blankNameResult = blankNameResponse.getBody();
        assertNotNull(blankNameResult);
        assertFalse(blankNameResult.mutated());
        assertEquals("Property name is required", blankNameResult.reason());
    }

    @Test
    void mutate_shouldMutate_whenNewValueIsEmpty() throws InterruptedException {
        Map<?, ?> initialResponse = restTemplate.getForObject("/actuator/env/notEmpty.property", Map.class);
        assertNotNull(initialResponse);
        Map<?, ?> property = (Map<?, ?>) initialResponse.get("property");
        assertNotNull(property);
        String initialValue = (String) property.get("value");
        assertFalse(initialValue.isBlank());
        assertTrue(initialValue.contains("not-empty"));

        mutateProperty("notEmpty.property", " ");

        Map<?, ?> updatedResponse = restTemplate.getForObject("/actuator/env/notEmpty.property", Map.class);
        assertNotNull(updatedResponse);
        Map<?, ?> updatedProperty = (Map<?, ?>) updatedResponse.get("property");
        assertNotNull(updatedProperty);
        String updatedValue = (String) updatedProperty.get("value");
        assertTrue(updatedValue.isBlank());
    }

    @Test
    void mutate_shouldReturnError_whenPropertyNameIsEmpty() {
        ResponseEntity<MutationResponse> response =
                restTemplate.postForEntity(path("/"), defaultEntity(), MutationResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Mutates the value of a property using the custom Actuator endpoint and waits for the update to take effect.
     *
     * @param propertyName name of the property to mutate
     * @param newValue     new value to set (can be blank or non-blank)
     */
    private void mutateProperty(String propertyName, String newValue) throws InterruptedException {
        ResponseEntity<MutationResponse> response = restTemplate.postForEntity(
                path("/" + propertyName + "?newValue=" + newValue), defaultEntity(), MutationResponse.class);

        TimeUnit.SECONDS.sleep(7); // wait for context update
        assertEquals(HttpStatus.OK, response.getStatusCode());

        MutationResponse result = response.getBody();
        assertNotNull(result);
        assertTrue(result.mutated());
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
     * Helper to construct a relative path to the property-management actuator endpoint.
     */
    private String path(String relative) {
        return "/actuator/property-management" + relative;
    }
}
