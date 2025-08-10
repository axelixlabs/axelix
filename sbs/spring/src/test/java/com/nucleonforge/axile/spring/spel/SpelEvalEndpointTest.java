package com.nucleonforge.axile.spring.spel;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.Main;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SpelEvalEndpoint}
 *
 * @author Nikita Kirillov
 * @since 10.08.2025
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
class SpelEvalEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnOkWithJavaVersion_WhenAccessingAllowedSystemPropertiesBean() {
        String expectedJavaVersion = System.getProperty("java.version");

        String spelExpression = "@systemProperties['java.version']";
        SpelEvaluationRequest request = new SpelEvaluationRequest(spelExpression);

        ResponseEntity<SpelEvaluationResponse> response =
                restTemplate.postForEntity(path(), createRequestEntity(request), SpelEvaluationResponse.class);

        assertThat(response)
                .returns(HttpStatus.OK, ResponseEntity::getStatusCode)
                .extracting(ResponseEntity::getBody)
                .isNotNull();

        assertThat(response.getBody()).returns(expectedJavaVersion, SpelEvaluationResponse::result);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideStatusOkTestCases")
    void shouldReturnOk_WhenRestrictedOperationAttempted(
            String testName, String spelExpression, boolean expectedEvaluateStatus, String expectedEvaluateResult) {
        SpelEvaluationRequest request = new SpelEvaluationRequest(spelExpression);

        ResponseEntity<SpelEvaluationResponse> response =
                restTemplate.postForEntity(path(), createRequestEntity(request), SpelEvaluationResponse.class);

        assertThat(response)
                .returns(HttpStatus.OK, ResponseEntity::getStatusCode)
                .extracting(ResponseEntity::getBody)
                .isNotNull();

        assertThat(response.getBody()).isNotNull().returns(expectedEvaluateResult, SpelEvaluationResponse::result);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideBadRequestTestCases")
    void shouldReturnBadRequest_WhenRestrictedOperationAttempted(
            String testName, String spelExpression, String firstExpectedError, String secondExpectedError) {
        SpelEvaluationRequest request = new SpelEvaluationRequest(spelExpression);

        ResponseEntity<SpelException> response =
                restTemplate.postForEntity(path(), createRequestEntity(request), SpelException.class);

        assertThat(response)
                .returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode)
                .extracting(ResponseEntity::getBody)
                .isNotNull()
                .satisfies(exception -> {
                    assertThat(exception)
                            .isInstanceOf(SpelException.class)
                            .hasMessageContaining(firstExpectedError)
                            .hasMessageContaining(secondExpectedError);
                });
    }

    private static Stream<Arguments> provideStatusOkTestCases() {
        return Stream.of(
                // Attempt to access to class Integer
                Arguments.of(
                        "Should return OK when allowed referenced to class Integer",
                        "T(java.lang.Integer).valueOf('123')",
                        true,
                        "123"),
                // Attempt to access read application name
                Arguments.of(
                        "Should return OK when allowed environment referenced",
                        "@environment.getProperty('spring.application.name')",
                        true,
                        "axile-sbs-spring-test"));
    }

    private static Stream<Arguments> provideBadRequestTestCases() {
        return Stream.of(
                // Attempt to access non-existing bean
                Arguments.of(
                        "Should return BAD_REQUEST when non-existent bean referenced",
                        "@nonExistingContextBean",
                        "Error evaluating expression: ",
                        "'Bean 'nonExistingContextBean' not found in application context'"),
                // Attempt to access restricted bean
                Arguments.of(
                        "Should return BAD_REQUEST when non-allowed bean referenced",
                        "@nonAllowedBean",
                        "Error evaluating expression: ",
                        "Access to bean 'nonAllowedBean' is denied. Only beans listed in the application properties/yaml are allowed."),
                // Attempt to call dangerous System method
                Arguments.of(
                        "Should return BAD_REQUEST when System class access attempted",
                        "T(java.lang.System).exit(0)",
                        "Error evaluating expression: ",
                        "EL1005E: Type cannot be found 'Class [java.lang.System] is not allowed for SpEL expressions"),
                // Attempt to create JDBC connection
                Arguments.of(
                        "Should return BAD_REQUEST when JDBC connection attempted",
                        "T(java.sql.DriverManager).getConnection('jdbc:h2:mem:test')",
                        "Error evaluating expression: ",
                        "EL1005E"),
                // Malformed SpEL syntax validation
                Arguments.of(
                        "Should return BAD_REQUEST with parse error when invalid SpEL syntax provided",
                        "@bean[missingBracket",
                        "Failed to parse expression: ",
                        "EL1044E"),
                // Attempt to call Integer class constructor
                Arguments.of(
                        "Should return BAD_REQUEST when Integer constructor called",
                        "new java.lang.Integer(123)",
                        "EL1002E",
                        "Constructor call: No suitable constructor found on type java.lang.Integer"),
                // Attempt to call ProcessBuilder class constructor
                Arguments.of(
                        "Should return BAD_REQUEST when ProcessBuilder constructor called",
                        "new java.lang.ProcessBuilder(\"comand\")",
                        "EL1002E",
                        "Constructor call: No suitable constructor found on type java.lang.ProcessBuilder"),
                // Attempt SpEL injection
                Arguments.of(
                        "Should return BAD_REQUEST when SpEL injection attempted",
                        "T(java.lang.Integer).valueOf('123')" + ".getClass().forName('java.lang.System')"
                                + ".getMethod('exit', T(int))"
                                + ".invoke(null, 0)",
                        "Error evaluating expression: ",
                        "EL1031E: Problem locating method getClass() on type java.lang.Integer"),
                // Attempt to call Runtime exit method
                Arguments.of(
                        "Should return BAD_REQUEST when Runtime exit method called",
                        "T(java.lang.Runtime).getRuntime().exit(0)",
                        "Error evaluating expression: ",
                        " EL1005E: Type cannot be found 'Class [java.lang.Runtime] is not allowed for SpEL expressions"),
                // Attempt to create infinite loop
                Arguments.of(
                        "Should return BAD_REQUEST when infinite loop attempted",
                        "while(true) { T(java.lang.System).currentTimeMillis() }",
                        "Failed to parse expression: ",
                        "EL1041E: After parsing a valid expression, there is still more data in the expression: 'lcurly({)'"),
                // Attempt to access java.lang.reflect.Method
                Arguments.of(
                        "Should return BAD_REQUEST when java.lang.reflect.Method accessed",
                        "T(java.lang.reflect.Method).getDeclaredMethods()",
                        "Error evaluating expression: ",
                        "EL10"),
                // Attempt to access java.lang.invoke.MethodHandles
                Arguments.of(
                        "Should return BAD_REQUEST when java.lang.invoke.MethodHandles accessed",
                        "T(java.lang.invoke.MethodHandles).lookup()",
                        "Error evaluating expression: ",
                        "EL10"));
    }

    private HttpEntity<SpelEvaluationRequest> createRequestEntity(SpelEvaluationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    private String path() {
        return "/actuator/spel-eval";
    }
}
