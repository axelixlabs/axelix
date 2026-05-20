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
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.axelixlabs.axelix.sbs.spring.core.master.CommitIdPluginShortBuildInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.NoOpShortBuildInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.ShortBuildInfoProvider;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ShortBuildInfoProviderAutoConfiguration}
 *
 * @since 10.02.2026
 * @author Nikita Kirillov
 */
class ShortBuildInfoProviderAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.info.git.location=classpath:other/git.properties")
            .withConfiguration(AutoConfigurations.of(
                    ShortBuildInfoProviderAutoConfiguration.class, ProjectInfoAutoConfiguration.class));

    @Test
    void shouldCreateCommitIdPluginProviderWhenGitPropertiesBeanExists() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ShortBuildInfoProvider.class);
            assertThat(context)
                    .getBean(ShortBuildInfoProvider.class)
                    .isInstanceOf(CommitIdPluginShortBuildInfoProvider.class);
            assertThat(context).doesNotHaveBean(NoOpShortBuildInfoProvider.class);
        });
    }

    @Test
    void shouldCreateNoOpProvider_whenRequiredFileNotExist() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        ShortBuildInfoProviderAutoConfiguration.class, ProjectInfoAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(ShortBuildInfoProvider.class);
                    assertThat(context)
                            .getBean(ShortBuildInfoProvider.class)
                            .isInstanceOf(NoOpShortBuildInfoProvider.class);
                    assertThat(context).doesNotHaveBean(CommitIdPluginShortBuildInfoProvider.class);
                });
    }
}
