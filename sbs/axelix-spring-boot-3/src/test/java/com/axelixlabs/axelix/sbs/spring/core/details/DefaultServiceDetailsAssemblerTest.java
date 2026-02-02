/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.details;

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

import com.axelixlabs.axelix.common.api.InstanceDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.BuildDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.GitDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.OsDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.RuntimeDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.SpringDetails;
import com.axelixlabs.axelix.sbs.spring.core.master.CommitIdPluginGitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.CycloneDXSBOMLibraryDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.master.GitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.LibraryDiscoverer;

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
        InstanceDetails result = serviceDetailsAssembler.assemble();

        assertThat(result).isNotNull();

        GitDetails git = result.getGit();
        assertThat(git.getCommitShaShort()).isEqualTo("a8b0929");
        assertThat(git.getBranch()).isEqualTo("main");
        assertThat(git.getCommitAuthor().getName()).isEqualTo("Mikhail Polivakha");
        assertThat(git.getCommitAuthor().getEmail()).isEqualTo("mikhailpolivakha@email.com");
        assertThat(git.getCommitTimestamp()).isEqualTo("1761249922000");

        SpringDetails spring = result.getSpring();
        assertThat(spring).isNotNull();
        assertThat(spring.getSpringBootVersion()).isNotBlank();
        assertThat(spring.getSpringFrameworkVersion()).isNotBlank();
        assertThat(spring.getSpringCloudVersion()).isNull();

        RuntimeDetails runtime = result.getRuntime();
        assertThat(runtime).isNotNull();
        assertThat(runtime.getJavaVersion()).isNotBlank();
        assertThat(runtime.getJdkVendor()).isNotBlank();
        assertThat(runtime.getGarbageCollector()).isNotBlank();
        assertThat(runtime.getKotlinVersion()).isNull();

        BuildDetails build = result.getBuild();
        assertThat(build.getArtifact()).isEqualTo("axelix-sbs");
        assertThat(build.getVersion()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(build.getGroup()).isEqualTo("com.axelixlabs.axelix");
        assertThat(build.getTime()).isEqualTo("2025-10-30T09:10:13.428Z");

        OsDetails os = result.getOs();
        assertThat(os).isNotNull();
        assertThat(os.getName()).isNotBlank();
        assertThat(os.getVersion()).isNotBlank();
        assertThat(os.getArch()).isNotBlank();
    }

    @TestConfiguration
    static class DefaultServiceDetailsAssemblerTestConfig {

        @Bean
        @Primary
        public BuildProperties buildProperties() {
            Properties props = new Properties();
            props.setProperty("group", "com.axelixlabs.axelix");
            props.setProperty("artifact", "axelix-sbs");
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
        public ServiceDetailsAssembler serviceDetailsAssembler(
                GitInformationProvider gitInformationProvider,
                ObjectProvider<BuildProperties> providerBuildProperties,
                LibraryDiscoverer libraryDiscoverer) {
            return new DefaultServiceDetailsAssembler(
                    gitInformationProvider, providerBuildProperties, libraryDiscoverer);
        }
    }
}
