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
package com.axelixlabs.axelix.sbs.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.gclog.ConditionalOnJcmd;
import com.axelixlabs.axelix.sbs.spring.gclog.DefaultGcLogService;
import com.axelixlabs.axelix.sbs.spring.gclog.GcLogEndpoint;
import com.axelixlabs.axelix.sbs.spring.gclog.JcmdExecutor;

/**
 * Auto-configuration for GC Log Endpoint functionality.
 *
 * <p>Configuration is conditional on the presence of JCMD utility and will not activate
 * if JCMD is not available in the system PATH.</p>
 *
 * @since 26.12.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
@ConditionalOnJcmd
public class GcLogEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JcmdExecutor jcmdExecutor() {
        return new JcmdExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultGcLogService gcLogService(JcmdExecutor jcmdExecutor) {
        return new DefaultGcLogService(jcmdExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    public GcLogEndpoint gcLogEndpoint(DefaultGcLogService gcLogService) {
        return new GcLogEndpoint(gcLogService);
    }
}
