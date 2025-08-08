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

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(initialResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isEqualTo("");

        String newValue = "new-value";
        mutateProperty("myEmpty.property", newValue);

        Map<?, ?> updatedResponse = restTemplate.getForObject("/actuator/env/myEmpty.property", Map.class);

        assertThat(updatedResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isEqualTo("new-value");
    }

    @Test
    void mutate_shouldNotMutate_whenPropertyNameIsBlank() {
        ResponseEntity<MutationResponse> blankNameResponse =
                restTemplate.postForEntity(path("/ \t?newValue=value"), defaultEntity(), MutationResponse.class);
        assertThat(blankNameResponse)
                .isNotNull()
                .returns(HttpStatus.OK, ResponseEntity::getStatusCode)
                .extracting(ResponseEntity::getBody)
                .isNotNull()
                .returns(false, MutationResponse::mutated)
                .returns("Property name is required", MutationResponse::reason);
    }

    @Test
    void mutate_shouldMutate_whenNewValueIsEmpty() throws InterruptedException {
        Map<?, ?> initialResponse = restTemplate.getForObject("/actuator/env/notEmpty.property", Map.class);
        assertThat(initialResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isEqualTo("not-empty");

        mutateProperty("notEmpty.property", " ");

        Map<?, ?> updatedResponse = restTemplate.getForObject("/actuator/env/notEmpty.property", Map.class);
        assertThat(updatedResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isInstanceOf(String.class)
                .isEqualTo(" ");
    }

    @Test
    void mutate_shouldReturnError_whenPropertyNameIsEmpty() {
        ResponseEntity<MutationResponse> response =
                restTemplate.postForEntity(path("/"), defaultEntity(), MutationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
        assertThat(response)
                .isNotNull()
                .returns(HttpStatus.OK, ResponseEntity::getStatusCode)
                .extracting(ResponseEntity::getBody)
                .isNotNull()
                .returns(true, MutationResponse::mutated);
    }

    private HttpEntity<Void> defaultEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    private String path(String relative) {
        return "/actuator/property-management" + relative;
    }
}
