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
package com.axelixlabs.axelix.sbs.autoconfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.master.CommitIdPluginShortBuildInfoProvider;
import com.axelixlabs.axelix.sbs.spring.master.GitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.master.NoOpGitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.master.NoOpShortBuildInfoProvider;
import com.axelixlabs.axelix.sbs.spring.master.ShortBuildInfoProvider;

/**
 * Auto-Configuration for registering the appropriate {@link ShortBuildInfoProvider} instances.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration(after = ProjectInfoAutoConfiguration.class)
public class ShortBuildInfoProviderAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ShortBuildInfoProviderAutoConfiguration.class);

    @Bean
    @ConditionalOnBean(GitProperties.class)
    public ShortBuildInfoProvider commitIdPluginShortBuildInfoProvider(GitProperties gitProperties) {
        return new CommitIdPluginShortBuildInfoProvider(gitProperties);
    }

    @Bean
    @ConditionalOnMissingBean(ShortBuildInfoProvider.class)
    public ShortBuildInfoProvider noOpShortBuildInfoProvider() {
        log.warn(
                """
            The {} is active. It practically means that the build information (the version of the build,
            the timestamp when app was build) will not be determined. If you see this message,
            then we were not able to find any valid {} that is going to work in your setup.
            """,
                NoOpGitInformationProvider.class.getSimpleName(),
                GitInformationProvider.class.getSimpleName());
        return new NoOpShortBuildInfoProvider();
    }
}
