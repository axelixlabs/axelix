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
package com.axelixlabs.axelix.master.service.transport;

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesEntry;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.master.domain.auth.OssAuthority;
import com.axelixlabs.axelix.master.domain.auth.SecurityContextExecutor;
import com.axelixlabs.axelix.master.service.auth.jwt.UserJwtDecoderService;

/**
 * Sanitizes sensitive property values according to the current user's permissions.
 *
 * @author Nikita Kirillov
 */
@Component
@SuppressWarnings("removal")
public class PropertySanitizer {
    static final String SANITIZED_VALUE = "******";

    private final SecurityContextExecutor securityContextExecutor;
    private final UserJwtDecoderService decoder;

    public PropertySanitizer(SecurityContextExecutor securityContextExecutor, UserJwtDecoderService decoder) {
        this.securityContextExecutor = securityContextExecutor;
        this.decoder = decoder;
    }

    /**
     * Sanitizes sensitive environment values unless the current user has permission to read them.
     *
     * @param feed the environment feed
     */
    public void sanitize(EnvironmentFeed feed) {
        if (canRead(OssAuthority.ENV_VALUES_READ)) {
            return;
        }
        feed.getPropertySources()
                .forEach(source -> source.getProperties()
                        .forEach(property -> property.setValue(mask(property.getSensitive(), property.getValue()))));
    }

    /**
     * Sanitizes sensitive configuration values unless the current user has permission to read them.
     *
     * @param feed the configuration properties feed
     */
    public void sanitize(ConfigurationPropertiesFeed feed) {
        if (canRead(OssAuthority.CONFIG_PROPS_VALUES_READ)) {
            return;
        }
        feed.getBeans().forEach(bean -> {
            mask(bean.getProperties());
            mask(bean.getInputs());
        });
    }

    private boolean canRead(OssAuthority permission) {
        return securityContextExecutor
                .getSecurityContext()
                .map(context -> decoder.decodeTokenToUser(context.token()).getRoles().stream()
                        .flatMap(role -> role.getEffectiveAuthorities().stream())
                        .anyMatch(authority -> permission.getCodename().equals(authority.getName())))
                .orElse(false);
    }

    private void mask(List<ConfigurationPropertiesEntry> entries) {
        for (ConfigurationPropertiesEntry entry : entries) {
            entry.setValue(mask(entry.getSensitive(), entry.getValue()));
        }
    }

    private @Nullable String mask(@Nullable Boolean sensitive, @Nullable String value) {
        return Boolean.TRUE.equals(sensitive) && value != null ? SANITIZED_VALUE : value;
    }
}
