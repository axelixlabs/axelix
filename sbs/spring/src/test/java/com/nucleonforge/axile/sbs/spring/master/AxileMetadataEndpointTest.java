package com.nucleonforge.axile.sbs.spring.master;

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
    AxileMetadataEndpoint.class,
    AxileMetadataEndpointTest.CurrentConfig.class,
    DefaultServiceMetadataAssembler.class,
    CommitIdPluginGitInformationProvider.class,
    CommitIdPluginShortBuildInfoProvider.class
})
class AxileMetadataEndpointTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private HealthEndpoint healthEndpoint;

    @TestConfiguration
    static class CurrentConfig {

        @Bean
        CycloneDXSBOMLibraryDiscoverer cycloneDXSBOMLibraryDiscoverer() {
            return new CycloneDXSBOMLibraryDiscoverer(new ClassPathResource("other/application.cdx.json"));
        }
    }

    @Test
    void shouldReceiveServiceMetadata() {

        Mockito.when(healthEndpoint.health()).thenReturn(Health.up().build());

        // when.
        ResponseEntity<String> result = testRestTemplate.getForEntity("/actuator/axile-metadata", String.class);

        // then.
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        JsonAssertions.assertThatJson(result.getBody())
                // we do not want to know exactly the java version on which the test is going to run
                .whenIgnoringPaths("javaVersion")
                .isEqualTo(
                        // language=json
                        """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "commitShortSha" : "a8b0929",
              "javaVersion" : "17.0.15",
              "springBootVersion" : "3.5.0",
              "healthStatus" : "UP"
            }
            """);
    }
}
