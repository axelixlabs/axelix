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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.axelixlabs.axelix.sbs.spring.core.master.CommitIdPluginGitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultLibraryInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.GitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.LibraryInformationProvider;

/**
 * Test configuration providing the service details related beans. It is used by both
 * {@link DefaultServiceDetailsAssemblerTest} and the shared endpoint test context (the
 * {@link GitInformationProvider} and {@link LibraryInformationProvider} beans defined here are
 * also consumed by the metadata endpoint test).
 *
 * @author Nikita Kirillov
 */
@TestConfiguration
@Import(AxelixDetailsEndpoint.class)
public class DetailsEndpointTestConfiguration {

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
    public LibraryInformationProvider libraryInformationProvider() {
        return new DefaultLibraryInformationProvider();
    }

    @Bean
    public ServiceDetailsAssembler serviceDetailsAssembler(
            GitInformationProvider gitInformationProvider,
            ObjectProvider<BuildProperties> providerBuildProperties,
            LibraryInformationProvider libraryInformationProvider) {
        return new DefaultServiceDetailsAssembler(
                gitInformationProvider, providerBuildProperties, libraryInformationProvider);
    }
}
