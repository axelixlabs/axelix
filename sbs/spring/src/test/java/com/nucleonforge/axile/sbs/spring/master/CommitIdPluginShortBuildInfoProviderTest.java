/*
 * Copyright 2025-present, Nucleon Forge Software.
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
