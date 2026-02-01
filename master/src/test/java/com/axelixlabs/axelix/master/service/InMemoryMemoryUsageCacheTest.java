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
package com.axelixlabs.axelix.master.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.domain.InstanceId;

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
    void shouldGetHeapSizeForExistingService() {
        // given.
        InstanceId instanceId = InstanceId.of("1");
        double rssUsage = 150d;
        subject.putHeapSize(instanceId, rssUsage);

        // when.
        double result = subject.getHeapSize(instanceId);

        // then.
        Assertions.assertThat(result).isEqualTo(rssUsage);
    }

    @Test
    void shouldGetHeapSizeForNonRegisteredService() {
        // given.
        InstanceId instanceId = InstanceId.of("1");

        // when.
        double result = subject.getHeapSize(instanceId);

        // then.
        Assertions.assertThat(result).isEqualTo(-1d);
    }

    @Test
    void shouldReturnAverageRssUsage() {
        // given.
        subject.putHeapSize(InstanceId.of("1"), 150d);
        subject.putHeapSize(InstanceId.of("2"), 440d);
        subject.putHeapSize(InstanceId.of("3"), 333d);

        // when.
        double result = subject.getAverageHeapSize();

        // then.
        Assertions.assertThat(result).isCloseTo((150d + 440d + 333d) / 3, Percentage.withPercentage(0.5d));
    }
}
