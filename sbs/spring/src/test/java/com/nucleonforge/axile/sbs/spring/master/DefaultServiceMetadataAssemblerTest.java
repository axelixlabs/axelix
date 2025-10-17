package com.nucleonforge.axile.sbs.spring.master;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;

import com.nucleonforge.axile.common.api.registration.ServiceMetadata;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultServiceMetadataAssembler}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
@Import({
    CommitIdPluginGitInformationProvider.class,
    CommitIdPluginShortBuildInfoProvider.class,
    DefaultServiceMetadataAssembler.class,
    DefaultServiceMetadataAssemblerTest.CurrentConfig.class
})
class DefaultServiceMetadataAssemblerTest {

    @Autowired
    private DefaultServiceMetadataAssembler subject;

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
    void shouldAssembleTheMetadataAboutGivenService() {
        // then.
        Mockito.when(healthEndpoint.health()).thenReturn(Health.up().build());

        // when.
        ServiceMetadata serviceMetadata = subject.assemble();

        // then.
        assertThat(serviceMetadata.commitShortSha()).isEqualTo("a8b0929");
        assertThat(serviceMetadata.serviceVersion()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(serviceMetadata.javaVersion()).isEqualTo(System.getProperty("java.version"));
        assertThat(serviceMetadata.version()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(serviceMetadata.springBootVersion()).isEqualTo("3.5.0");
        assertThat(serviceMetadata.healthStatus()).isEqualTo(ServiceMetadata.HealthStatus.UP);
    }
}
