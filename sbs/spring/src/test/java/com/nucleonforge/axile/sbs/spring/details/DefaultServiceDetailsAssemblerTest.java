package com.nucleonforge.axile.sbs.spring.details;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import com.nucleonforge.axile.common.api.AxileDetails;
import com.nucleonforge.axile.common.api.AxileDetails.BuildDetails;
import com.nucleonforge.axile.common.api.AxileDetails.GitDetails;
import com.nucleonforge.axile.common.api.AxileDetails.OsDetails;
import com.nucleonforge.axile.common.api.AxileDetails.RuntimeDetails;
import com.nucleonforge.axile.common.api.AxileDetails.SpringDetails;
import com.nucleonforge.axile.sbs.spring.master.CommitIdPluginGitInformationProvider;
import com.nucleonforge.axile.sbs.spring.master.CycloneDXSBOMLibraryDiscoverer;
import com.nucleonforge.axile.sbs.spring.master.GitInformationProvider;
import com.nucleonforge.axile.sbs.spring.master.LibraryDiscoverer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultServiceDetailsAssembler}.
 *
 * @since 30.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest
@Import(DefaultServiceDetailsAssemblerTest.DefaultServiceDetailsAssemblerTestConfig.class)
class DefaultServiceDetailsAssemblerTest {

    @Autowired
    private ServiceDetailsAssembler serviceDetailsAssembler;

    @Test
    void shouldAssembleCompleteServiceDetails() {
        AxileDetails result = serviceDetailsAssembler.assemble();

        assertThat(result).isNotNull();

        GitDetails git = result.git();
        assertThat(git.commitShaShort()).isEqualTo("a8b0929");
        assertThat(git.branch()).isEqualTo("main");
        assertThat(git.commitAuthor().name()).isEqualTo("Mikhail Polivakha");
        assertThat(git.commitAuthor().email()).isEqualTo("mikhailpolivakha@email.com");
        assertThat(git.commitTimestamp()).isEqualTo("2025-09-28T13:50:13+03:00");

        SpringDetails spring = result.spring();
        assertThat(spring).isNotNull();
        assertThat(spring.springBootVersion()).isNotBlank();
        assertThat(spring.springFrameworkVersion()).isNotBlank();
        assertThat(spring.springCloudVersion()).isBlank();

        RuntimeDetails runtime = result.runtime();
        assertThat(runtime).isNotNull();
        assertThat(runtime.javaVersion()).isNotBlank();
        assertThat(runtime.jdkVendor()).isNotBlank();
        assertThat(runtime.garbageCollector()).isNotBlank();
        assertThat(runtime.kotlinVersion()).isBlank();

        BuildDetails build = result.build();
        assertThat(build.artifact()).isEqualTo("axile-sbs");
        assertThat(build.version()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(build.group()).isEqualTo("com.nucleonforge.axile");
        assertThat(build.time()).isEqualTo("2025-10-30T09:10:13.428Z");

        OsDetails os = result.os();
        assertThat(os).isNotNull();
        assertThat(os.name()).isNotBlank();
        assertThat(os.version()).isNotBlank();
        assertThat(os.arch()).isNotBlank();
    }

    @TestConfiguration
    static class DefaultServiceDetailsAssemblerTestConfig {

        @Bean
        @Primary
        public BuildProperties buildProperties() {
            Properties props = new Properties();
            props.setProperty("group", "com.nucleonforge.axile");
            props.setProperty("artifact", "axile-sbs");
            props.setProperty("version", "1.0.0-SNAPSHOT");
            props.setProperty("name", "test-application");
            props.setProperty("time", "2025-10-30T09:10:13.428Z");

            return new BuildProperties(props);
        }

        @Bean
        public GitInformationProvider gitInformationProvider(GitProperties gitProperties) {
            return new CommitIdPluginGitInformationProvider(gitProperties);
        }

        @Bean
        public LibraryDiscoverer libraryDiscoverer() {
            return new CycloneDXSBOMLibraryDiscoverer(new ClassPathResource("other/application.cdx.json"));
        }

        @Bean
        public DefaultServiceDetailsAssembler defaultServiceDetailsAssembler(
                GitInformationProvider gitInformationProvider,
                ObjectProvider<BuildProperties> providerBuildProperties,
                LibraryDiscoverer libraryDiscoverer) {
            return new DefaultServiceDetailsAssembler(
                    gitInformationProvider, providerBuildProperties, libraryDiscoverer);
        }
    }
}
