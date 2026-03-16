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
package com.axelixlabs.axelix.sbs.spring.core.config;

import java.time.Duration;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Default implementation of the {@link AbstractSelfRegistrationConfigurationProperties}.
 *
 * @since 05.02.2026
 * @author Nikita Kirillov
 * @author Sergey Ckerkasov
 */
@ConfigurationProperties(prefix = "axelix.sbs.discovery")
public class DefaultSelfRegistrationConfigurationProperties extends AbstractSelfRegistrationConfigurationProperties {

    public DefaultSelfRegistrationConfigurationProperties(
            String masterUrl, String instanceUrl, String instanceName, @Nullable Duration heartbeatInterval) {
        super(masterUrl, instanceUrl, instanceName, heartbeatInterval);
    }
}
