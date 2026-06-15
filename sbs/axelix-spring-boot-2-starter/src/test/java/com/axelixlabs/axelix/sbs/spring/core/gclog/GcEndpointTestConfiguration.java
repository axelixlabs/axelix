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
package com.axelixlabs.axelix.sbs.spring.core.gclog;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration for {@link AxelixGcEndpointTest}, part of the shared endpoint test context.
 */
@TestConfiguration
public class GcEndpointTestConfiguration {

    @Bean
    public JcmdExecutor jcmdExecutor() {
        return new JcmdExecutor();
    }

    @Bean
    public GcLogService gcLogService(JcmdExecutor jcmdExecutor) {
        return new DefaultGcLogService(jcmdExecutor);
    }

    @Bean
    public AxelixGcEndpoint gcLogEndpoint(GcLogService gcLogService) {
        return new AxelixGcEndpoint(gcLogService);
    }
}
