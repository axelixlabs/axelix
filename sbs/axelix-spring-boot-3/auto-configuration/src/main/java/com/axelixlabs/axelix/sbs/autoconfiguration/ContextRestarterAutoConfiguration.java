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

import com.axelixlabs.axelix.sbs.spring.context.ContextRestarter;
import com.axelixlabs.axelix.sbs.spring.context.DefaultContextRestarter;
import com.axelixlabs.axelix.sbs.spring.context.RestartListener;

/**
 * Auto-configuration for context restart support.
 *
 * <p>This configuration registers beans that handle application context restart events.
 * It provides a {@link ContextRestarter} bean responsible for triggering context restarts,
 * and a {@link RestartListener} bean that listens for restart events.</p>
 *
 * @since 10.07.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class ContextRestarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ContextRestarter contextRestarter() {
        return new DefaultContextRestarter();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestartListener restartListener() {
        return new RestartListener();
    }
}
