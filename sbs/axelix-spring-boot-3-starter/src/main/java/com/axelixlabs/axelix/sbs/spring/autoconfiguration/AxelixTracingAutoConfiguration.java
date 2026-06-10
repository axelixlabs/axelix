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

import io.micrometer.tracing.Tracer;

import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.boot.actuate.autoconfigure.tracing.MicrometerTracingAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.transactions.AxelixTransactionTracer;
import com.axelixlabs.axelix.sbs.spring.core.transactions.DefaultAxelixTransactionTracer;

/**
 * Auto-configuration for extended Axelix tracing components.
 *
 * @author Nikita Kirillov
 */
@AutoConfiguration(after = MicrometerTracingAutoConfiguration.class)
@ConditionalOnClass(Tracer.class)
@ConditionalOnEnabledTracing
public class AxelixTracingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(Tracer.class)
    public AxelixTransactionTracer axelixTransactionTracer(Tracer tracer) {
        return new DefaultAxelixTransactionTracer(tracer);
    }
}
