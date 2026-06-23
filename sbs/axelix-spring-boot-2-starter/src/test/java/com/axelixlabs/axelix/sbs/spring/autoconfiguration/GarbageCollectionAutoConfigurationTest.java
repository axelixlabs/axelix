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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.gclog.DefaultGcLogService;
import com.axelixlabs.axelix.sbs.spring.core.gclog.GcLogService;
import com.axelixlabs.axelix.sbs.spring.core.gclog.JcmdExecutor;
import com.axelixlabs.axelix.sbs.spring.core.log.SLF4JLogger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link GarbageCollectionAutoConfiguration}
 *
 * @since 22.06.2026
 * @author Mikhail Polivakha
 */
class GarbageCollectionAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GarbageCollectionAutoConfiguration.class));

    @Test
    void shouldCreateGcLogServiceBeansUnconditionally() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(JcmdExecutor.class);
            assertThat(context).hasSingleBean(GcLogService.class);
        });
    }

    @Test
    void shouldHandleCustomBeans() {
        contextRunner.withUserConfiguration(CustomGcLogServiceConfig.class).run(context -> {
            assertThat(context.getBean(GcLogService.class)).isExactlyInstanceOf(CustomGcLogService.class);
        });
    }

    @TestConfiguration
    static class CustomGcLogServiceConfig {
        @Bean
        public GcLogService gcLogService(JcmdExecutor jcmdExecutor) {
            return new CustomGcLogService(jcmdExecutor);
        }
    }

    static class CustomGcLogService extends DefaultGcLogService {
        public CustomGcLogService(JcmdExecutor jcmdExecutor) {
            super(jcmdExecutor, new SLF4JLogger(LoggerFactory.getLogger(DefaultGcLogService.class)));
        }
    }
}
