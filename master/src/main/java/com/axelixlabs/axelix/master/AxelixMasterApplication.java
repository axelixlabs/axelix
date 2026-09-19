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
package com.axelixlabs.axelix.master;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.otlp.OtlpMetricsExportAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClientAutoConfiguration;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.cloud.vault.config.VaultAutoConfiguration;
import org.springframework.cloud.vault.config.VaultHealthIndicatorAutoConfiguration;

/**
 * Composed {@link SpringBootApplication} meta-annotation shared by every Axelix Master entrypoint.
 * Centralizes the auto-configurations that Axelix replaces with its own wiring.
 *
 * @author Dmitry Mazurov
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootApplication(
        exclude = {
            CompositeDiscoveryClientAutoConfiguration.class,
            SimpleDiscoveryClientAutoConfiguration.class,
            OtlpMetricsExportAutoConfiguration.class,
            PrometheusMetricsExportAutoConfiguration.class,
            // We only need to use Vault at startup.
            VaultAutoConfiguration.class,
            // Since we don't keep a live Vault client, the indicator is not useful.
            VaultHealthIndicatorAutoConfiguration.class
        })
public @interface AxelixMasterApplication {}
