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
        assertThat(gitCommitInfo.commitTimestamp()).isEqualTo("2025-09-28T13:50:13+03:00");

        assertThat(gitCommitInfo.commitAuthor())
                .extracting(GitInfo.CommitAuthor::email)
                .isEqualTo("mikhailpolivakha@email.com");

        assertThat(gitCommitInfo.commitAuthor())
                .extracting(GitInfo.CommitAuthor::name)
                .isEqualTo("Mikhail Polivakha");
    }
}
