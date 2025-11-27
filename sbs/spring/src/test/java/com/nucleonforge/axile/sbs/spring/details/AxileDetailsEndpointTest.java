package com.nucleonforge.axile.sbs.spring.details;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.common.api.AxileDetails;
import com.nucleonforge.axile.common.api.AxileDetails.BuildDetails;
import com.nucleonforge.axile.common.api.AxileDetails.GitDetails;
import com.nucleonforge.axile.common.api.AxileDetails.OsDetails;
import com.nucleonforge.axile.common.api.AxileDetails.RuntimeDetails;
import com.nucleonforge.axile.common.api.AxileDetails.SpringDetails;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

/**
 * Integration tests for {@link AxileDetailsEndpoint}.
 *
 * @since 30.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"management.endpoints.web.exposure.include=axile-details"})
@Import({
    DefaultServiceDetailsAssemblerTest.DefaultServiceDetailsAssemblerTestConfig.class,
    AxileDetailsEndpointTest.AxileDetailsEndpointTestConfig.class
})
class AxileDetailsEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnValidDetailsStructure() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/axile-details", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        assertThatJson(responseBody).node("git").isNotNull();
        assertThatJson(responseBody)
                .inPath("git")
                .isObject()
                .contains(
                        entry("commitShaShort", "a8b0929"),
                        entry("branch", "main"),
                        entry("commitTimestamp", "1761249922000"))
                .containsKeys("commitAuthor", "commitTimestamp");

        assertThatJson(responseBody)
                .inPath("git.commitAuthor")
                .isObject()
                .contains(entry("name", "Mikhail Polivakha"), entry("email", "mikhailpolivakha@email.com"));

        assertThatJson(responseBody)
                .inPath("git.commitAuthor")
                .isObject()
                .containsOnly(entry("name", "Mikhail Polivakha"), entry("email", "mikhailpolivakha@email.com"));

        assertThatJson(responseBody)
                .inPath("spring")
                .isObject()
                .contains(entry("springBootVersion", "3.5.0"), entry("springFrameworkVersion", "6.2.7"));

        assertThatJson(responseBody)
                .inPath("runtime")
                .isObject()
                .containsKeys("javaVersion", "jdkVendor", "garbageCollector");

        assertThatJson(responseBody).node("build").isNotNull();
        assertThatJson(responseBody)
                .inPath("build")
                .isObject()
                .containsOnly(
                        entry("artifact", "axile-sbs"),
                        entry("version", "1.0.0-SNAPSHOT"),
                        entry("group", "com.nucleonforge.axile"),
                        entry("time", "2025-10-30T09:10:13.428Z"));

        assertThatJson(responseBody).inPath("os").isObject().containsOnlyKeys("name", "version", "arch");
    }

    @Test
    void shouldContainValidDetails() {
        ResponseEntity<AxileDetails> response =
                restTemplate.getForEntity("/actuator/axile-details", AxileDetails.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        AxileDetails details = response.getBody();
        assertThat(details).isNotNull();

        GitDetails git = details.git();
        assertThat(git).isNotNull();
        assertThat(git.commitShaShort()).isEqualTo("a8b0929");
        assertThat(git.branch()).isEqualTo("main");
        assertThat(git.commitAuthor().name()).isEqualTo("Mikhail Polivakha");
        assertThat(git.commitAuthor().email()).isEqualTo("mikhailpolivakha@email.com");
        assertThat(git.commitTimestamp()).isNotNull();

        SpringDetails spring = details.spring();
        assertThat(spring.springBootVersion()).isEqualTo("3.5.0");
        assertThat(spring.springFrameworkVersion()).isEqualTo("6.2.7");
        assertThat(spring.springCloudVersion()).isNull();

        RuntimeDetails runtime = details.runtime();
        assertThat(runtime).isNotNull();
        assertThat(runtime.javaVersion()).isNotBlank();
        assertThat(runtime.jdkVendor()).isNotBlank();
        assertThat(runtime.garbageCollector()).isNotBlank();
        assertThat(runtime.kotlinVersion()).isNull();

        BuildDetails build = details.build();
        assertThat(build).isNotNull();
        assertThat(build.artifact()).isEqualTo("axile-sbs");
        assertThat(build.version()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(build.group()).isEqualTo("com.nucleonforge.axile");
        assertThat(build.time()).isEqualTo("2025-10-30T09:10:13.428Z");

        OsDetails os = details.os();
        assertThat(os).isNotNull();
        assertThat(os.name()).isNotBlank();
        assertThat(os.version()).isNotBlank();
        assertThat(os.arch()).isNotBlank();
    }

    @TestConfiguration
    static class AxileDetailsEndpointTestConfig {

        @Bean
        public AxileDetailsEndpoint axileDetailsEndpoint(ServiceDetailsAssembler serviceDetailsAssembler) {
            return new AxileDetailsEndpoint(serviceDetailsAssembler);
        }
    }
}
