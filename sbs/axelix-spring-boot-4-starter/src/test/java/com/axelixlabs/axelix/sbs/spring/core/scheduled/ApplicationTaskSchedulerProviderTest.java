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
package com.axelixlabs.axelix.sbs.spring.core.scheduled;

import org.junit.jupiter.api.Test;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ApplicationTaskSchedulerProvider}.
 *
 * @since 20.10.2026
 * @author Dmitry Mazurov
 */
class ApplicationTaskSchedulerProviderTest {

    private final ApplicationTaskSchedulerProvider provider = new ApplicationTaskSchedulerProvider();

    @Test // GH-1726
    void shouldReturnSchedulerOfTheRegistrar_whenRegistrarHasScheduler() {
        // given
        TaskScheduler applicationScheduler = new ThreadPoolTaskScheduler();
        ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();
        registrar.setTaskScheduler(applicationScheduler);
        provider.configureTasks(registrar);

        // when
        TaskScheduler scheduler = provider.getTaskScheduler();

        // then
        assertThat(scheduler).isSameAs(applicationScheduler);
    }

    @Test // GH-1726
    void shouldThrow_whenThereIsNoRegistrar() {
        // given: configureTasks was never called, e.g. @EnableScheduling is absent

        // when, then
        assertThatThrownBy(provider::getTaskScheduler)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("@EnableScheduling");
    }

    @Test // GH-1726
    void shouldThrow_whenRegistrarHasNoScheduler() {
        // given
        provider.configureTasks(new ScheduledTaskRegistrar());

        // when, then
        assertThatThrownBy(provider::getTaskScheduler)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("@EnableScheduling");
    }
}
