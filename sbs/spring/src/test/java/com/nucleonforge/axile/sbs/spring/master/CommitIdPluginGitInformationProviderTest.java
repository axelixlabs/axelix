/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.master;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.nucleonforge.axile.common.api.registration.GitInfo;

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
