package com.nucleonforge.axile.sbs.spring.master;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.nucleonforge.axile.common.api.registration.ShortBuildInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link CommitIdPluginShortBuildInfoProvider}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
@Import({CommitIdPluginShortBuildInfoProvider.class, ProjectInfoAutoConfiguration.class})
class CommitIdPluginShortBuildInfoProviderTest {

    @Autowired
    private CommitIdPluginShortBuildInfoProvider subject;

    @Test
    void shouldAssembleShortBuildInfoFromPropertiesFile() {
        ShortBuildInfo shortBuildInfo = subject.getShortBuildInfo().orElse(null);

        assertThat(shortBuildInfo).isNotNull();
        assertThat(shortBuildInfo.serviceVersion()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(shortBuildInfo.buildTimestamp()).isEqualTo("2024-11-28T17:37:52+03:00");
    }
}
