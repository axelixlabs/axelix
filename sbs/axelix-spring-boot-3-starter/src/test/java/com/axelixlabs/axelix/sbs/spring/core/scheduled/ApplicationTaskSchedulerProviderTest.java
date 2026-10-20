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

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.task.TaskRejectedException;
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

    @AfterEach
    void tearDown() {
        provider.close();
    }

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
    void shouldFallBackToOwnScheduler_whenThereIsNoRegistrar() throws Exception {
        // given: configureTasks was never called, e.g. @EnableScheduling is absent

        // when
        TaskScheduler scheduler = provider.getTaskScheduler();

        // then
        assertThat(threadNameOf(scheduler)).startsWith(ApplicationTaskSchedulerProvider.FALLBACK_THREAD_NAME_PREFIX);
    }

    @Test // GH-1726
    void shouldFallBackToOwnScheduler_whenRegistrarHasNoScheduler() throws Exception {
        // given
        provider.configureTasks(new ScheduledTaskRegistrar());

        // when
        TaskScheduler scheduler = provider.getTaskScheduler();

        // then
        assertThat(threadNameOf(scheduler)).startsWith(ApplicationTaskSchedulerProvider.FALLBACK_THREAD_NAME_PREFIX);
    }

    @Test // GH-1726
    void shouldShutDownFallbackScheduler_whenClosed() {
        // given
        TaskScheduler scheduler = provider.getTaskScheduler();

        // when
        provider.close();

        // then
        assertThatThrownBy(() -> scheduler.schedule(() -> {}, Instant.now())).isInstanceOf(TaskRejectedException.class);
    }

    private static String threadNameOf(TaskScheduler scheduler) throws Exception {
        CompletableFuture<String> threadName = new CompletableFuture<>();

        scheduler.schedule(() -> threadName.complete(Thread.currentThread().getName()), Instant.now());

        return threadName.get(5, TimeUnit.SECONDS);
    }
}
