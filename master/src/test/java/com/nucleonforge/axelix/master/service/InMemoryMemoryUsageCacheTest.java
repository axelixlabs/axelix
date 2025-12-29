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
package com.nucleonforge.axelix.master.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axelix.master.model.instance.InstanceId;

/**
 * Unit tests for {@link InMemoryMemoryUsageCache}.
 *
 * @author Mikhail Polivakha
 */
class InMemoryMemoryUsageCacheTest {

    private InMemoryMemoryUsageCache subject;

    @BeforeEach
    void setUp() {
        subject = new InMemoryMemoryUsageCache();
    }

    @Test
    void shouldGetRssForExistingService() {
        // given.
        InstanceId instanceId = InstanceId.of("1");
        double rssUsage = 150d;
        subject.putRss(instanceId, rssUsage);

        // when.
        double result = subject.getRss(instanceId);

        // then.
        Assertions.assertThat(result).isEqualTo(rssUsage);
    }

    @Test
    void shouldGetRssForNonRegisteredService() {
        // given.
        InstanceId instanceId = InstanceId.of("1");

        // when.
        double result = subject.getRss(instanceId);

        // then.
        Assertions.assertThat(result).isEqualTo(-1d);
    }

    @Test
    void shouldReturnAverageRssUsage() {
        // given.
        subject.putRss(InstanceId.of("1"), 150d);
        subject.putRss(InstanceId.of("2"), 440d);
        subject.putRss(InstanceId.of("3"), 333d);

        // when.
        double result = subject.getAverageRss();

        // then.
        Assertions.assertThat(result).isCloseTo((150d + 440d + 333d) / 3, Percentage.withPercentage(0.5d));
    }
}
