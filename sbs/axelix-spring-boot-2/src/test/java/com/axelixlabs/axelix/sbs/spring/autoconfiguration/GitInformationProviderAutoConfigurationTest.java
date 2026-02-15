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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.info.GitProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.axelixlabs.axelix.sbs.spring.core.master.CommitIdPluginGitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.GitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.NoOpGitInformationProvider;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link GitInformationProviderAutoConfiguration}
 *
 * @since 09.02.2026
 * @author Nikita Kirillov
 */
class GitInformationProviderAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    GitInformationProviderAutoConfiguration.class, ProjectInfoAutoConfiguration.class));

    @Test
    void shouldCreateCommitIdPluginGitInformationProviderWhenGitPropertiesAvailable() {
        contextRunner
                .withPropertyValues(
                        "management.info.git.enabled=true", "spring.info.git.location=classpath:other/git.properties")
                .run(context -> {
                    assertThat(context).hasSingleBean(GitProperties.class);
                    assertThat(context).hasSingleBean(GitInformationProvider.class);
                    assertThat(context.getBean(GitInformationProvider.class))
                            .isExactlyInstanceOf(CommitIdPluginGitInformationProvider.class);
                    assertThat(context).doesNotHaveBean(NoOpGitInformationProvider.class);
                });
    }

    @Test
    void shouldCreateNoOpGitInformationProviderWhenGitPropertiesFileNotExist() {
        contextRunner
                .withPropertyValues(
                        "spring.info.git.location=classpath:not-exist-git.properties",
                        "management.info.git.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(GitInformationProvider.class);
                    assertThat(context.getBean(GitInformationProvider.class))
                            .isExactlyInstanceOf(NoOpGitInformationProvider.class);
                    assertThat(context).doesNotHaveBean(GitProperties.class);
                    assertThat(context).doesNotHaveBean(CommitIdPluginGitInformationProvider.class);
                });
    }

    @Test
    void shouldCreateNoOpGitInformationProviderWhenGitInfoDisabled() {
        contextRunner.withPropertyValues("management.info.git.enabled=false").run(context -> {
            assertThat(context).hasSingleBean(GitInformationProvider.class);
            assertThat(context.getBean(GitInformationProvider.class))
                    .isExactlyInstanceOf(NoOpGitInformationProvider.class);
            assertThat(context).doesNotHaveBean(GitProperties.class);
            assertThat(context).doesNotHaveBean(CommitIdPluginGitInformationProvider.class);
        });
    }
}
