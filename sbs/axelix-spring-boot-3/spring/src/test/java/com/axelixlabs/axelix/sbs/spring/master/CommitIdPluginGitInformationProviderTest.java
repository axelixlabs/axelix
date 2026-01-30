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
package com.axelixlabs.axelix.sbs.spring.master;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.common.api.registration.GitInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link CommitIdPluginGitInformationProvider}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
@Import({CommitIdPluginGitInformationProvider.class, ProjectInfoAutoConfiguration.class})
class CommitIdPluginGitInformationProviderTest {

    @Autowired
    private CommitIdPluginGitInformationProvider subject;

    @Test
    void shouldAssembleGitInfoFromPropertiesFile() {

        // when.
        GitInfo gitCommitInfo = subject.getGitCommitInfo().orElse(null);

        // then.
        assertThat(gitCommitInfo).isNotNull();
        assertThat(gitCommitInfo.branch()).isEqualTo("main");
        assertThat(gitCommitInfo.commitShaShort()).isEqualTo("a8b0929");
        assertThat(gitCommitInfo.commitTimestamp()).isEqualTo("1761249922000");

        assertThat(gitCommitInfo.commitAuthor())
                .extracting(GitInfo.CommitAuthor::email)
                .isEqualTo("mikhailpolivakha@email.com");

        assertThat(gitCommitInfo.commitAuthor())
                .extracting(GitInfo.CommitAuthor::name)
                .isEqualTo("Mikhail Polivakha");
    }
}
