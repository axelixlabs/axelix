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
package com.axelixlabs.axelix.sbs.spring.details;

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
import com.axelixlabs.axelix.sbs.spring.master.CommitIdPluginGitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.master.CycloneDXSBOMLibraryDiscoverer;
import com.axelixlabs.axelix.sbs.spring.master.GitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.master.LibraryDiscoverer;

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

        GitDetails git = result.git();
        assertThat(git.commitShaShort()).isEqualTo("a8b0929");
        assertThat(git.branch()).isEqualTo("main");
        assertThat(git.commitAuthor().name()).isEqualTo("Mikhail Polivakha");
        assertThat(git.commitAuthor().email()).isEqualTo("mikhailpolivakha@email.com");
        assertThat(git.commitTimestamp()).isEqualTo("1761249922000");

        SpringDetails spring = result.spring();
        assertThat(spring).isNotNull();
        assertThat(spring.springBootVersion()).isNotBlank();
        assertThat(spring.springFrameworkVersion()).isNotBlank();
        assertThat(spring.springCloudVersion()).isNull();

        RuntimeDetails runtime = result.runtime();
        assertThat(runtime).isNotNull();
        assertThat(runtime.javaVersion()).isNotBlank();
        assertThat(runtime.jdkVendor()).isNotBlank();
        assertThat(runtime.garbageCollector()).isNotBlank();
        assertThat(runtime.kotlinVersion()).isNull();

        BuildDetails build = result.build();
        assertThat(build.artifact()).isEqualTo("axelix-sbs");
        assertThat(build.version()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(build.group()).isEqualTo("com.axelixlabs.axelix");
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
        public DefaultServiceDetailsAssembler defaultServiceDetailsAssembler(
                GitInformationProvider gitInformationProvider,
                ObjectProvider<BuildProperties> providerBuildProperties,
                LibraryDiscoverer libraryDiscoverer) {
            return new DefaultServiceDetailsAssembler(
                    gitInformationProvider, providerBuildProperties, libraryDiscoverer);
        }
    }
}
