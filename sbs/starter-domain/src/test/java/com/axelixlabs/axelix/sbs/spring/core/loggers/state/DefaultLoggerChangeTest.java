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
package com.axelixlabs.axelix.sbs.spring.core.loggers.state;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultLoggerChange}.
 *
 * @author Mikhail Polivakha
 */
class DefaultLoggerChangeTest {

    @Test
    void shouldRunRollbackActionOnManualRollback() {
        // given.
        AtomicBoolean rollbackInvoked = new AtomicBoolean(false);
        DefaultLoggerChange subject =
                new DefaultLoggerChange(() -> rollbackInvoked.set(true), Duration.ofSeconds(30), "WARN");

        // when.
        subject.rollbackManually();

        // then.
        assertThat(rollbackInvoked).isTrue();
    }

    @Test
    void shouldNotRunScheduledRollbackAfterManualRollback() throws Exception {
        // given.
        AtomicInteger rollbackCount = new AtomicInteger();
        DefaultLoggerChange subject =
                new DefaultLoggerChange(rollbackCount::incrementAndGet, Duration.ofSeconds(1), "WARN");

        // when.
        subject.rollbackManually();

        // then.
        assertThat(rollbackCount.get()).isEqualTo(1);
        Thread.sleep(1_500);
        assertThat(rollbackCount.get()).isEqualTo(1);
    }
}
