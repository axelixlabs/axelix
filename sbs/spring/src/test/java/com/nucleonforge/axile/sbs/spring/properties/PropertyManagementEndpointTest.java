package com.nucleonforge.axile.sbs.spring.properties;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.nucleonforge.axile.sbs.spring.context.DefaultContextRestarter;
import com.nucleonforge.axile.sbs.spring.context.RestartListener;
import com.nucleonforge.axile.sbs.spring.utils.ContextKeepAliveTestListener;

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
        // spotless:off
        properties = {
            "myEmpty.property= ",
            "notEmpty.property=not-empty",
            "management.endpoint.env.show-values=always",
            "kebab-case.property=old-value"
        })
        // spotless:on
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({
    PropertyManagementEndpoint.class,
    ContextReloadingPropertyMutator.class,
    DefaultPropertyDiscoverer.class,
    DefaultContextRestarter.class,
    RestartListener.class
})
class PropertyManagementEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DefaultPropertyDiscoverer propertyDiscoverer;

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
    void mutate_shouldUpdateStandardPropertyValue() throws InterruptedException {
        Map<?, ?> initialResponse = restTemplate.getForObject("/actuator/env/kebab-case.property", Map.class);

        assertThat(initialResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isEqualTo("old-value");

        String newValue = "new-value";
        mutateProperty("kebab-case.property", newValue);

        Map<?, ?> updatedResponse = restTemplate.getForObject("/actuator/env/kebab-case.property", Map.class);

        assertThat(updatedResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isEqualTo("new-value");
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

        mutateProperty("notEmpty.property", "");

        Map<?, ?> updatedResponse = restTemplate.getForObject("/actuator/env/notEmpty.property", Map.class);

        assertThat(updatedResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isEqualTo("");
    }

    @Test
    void matate_shouldReturnBadRequest_whenPropertyNameIsEmpty() {
        PropertyMutationRequest request = new PropertyMutationRequest(" ", "someValue");

        ResponseEntity<Void> response = restTemplate.postForEntity(path(), defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode);
    }

    @Test
    void mutate_shouldReturnBadRequest_whenPropertyNameIsBlank() {
        PropertyMutationRequest request = new PropertyMutationRequest("\t", "someValue");

        ResponseEntity<Void> response = restTemplate.postForEntity(path(), defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode);
    }

    @Test
    void matate_shouldCreateNewProperty_whenPropertyDoesNotExist() throws InterruptedException {
        Map<?, ?> initialResponse =
                restTemplate.getForObject("/actuator/env/non-existent-property.property", Map.class);
        assertThat(initialResponse).isNull();

        mutateProperty("non-existent-property.property", "true");

        Map<?, ?> updatedResponse =
                restTemplate.getForObject("/actuator/env/non-existent-property.property", Map.class);

        assertThat(updatedResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isEqualTo("true");
    }

    @Test
    void matate_shouldCreateNewProperty_whenPropertyNameNonStandard() throws InterruptedException {
        Map<?, ?> initialResponse = restTemplate.getForObject("/actuator/env/non_$tandard.property", Map.class);

        assertThat(initialResponse).isNull();

        mutateProperty("non_$tandard.property", "someValue");

        Map<?, ?> updatedResponse = restTemplate.getForObject("/actuator/env/non_$tandard.property", Map.class);

        assertThat(updatedResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isEqualTo("someValue");
    }

    /**
     * Mutates the value of a property using the custom Actuator endpoint and waits for the update to take effect.
     *
     * @param propertyName name of the property to mutate
     * @param newValue     new value to set (can be blank or non-blank)
     */
    private void mutateProperty(String propertyName, String newValue) throws InterruptedException {
        PropertyMutationRequest request = new PropertyMutationRequest(propertyName, newValue);

        ResponseEntity<Void> response = restTemplate.postForEntity(path(), defaultEntity(request), Void.class);

        TimeUnit.SECONDS.sleep(7); // wait for context update
        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
    }

    private HttpEntity<PropertyMutationRequest> defaultEntity(PropertyMutationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    private String path() {
        return "/actuator/property-management";
    }
}
