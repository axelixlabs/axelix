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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;

/**
 * Service for retrieving configuration properties.
 *
 * @author Nikita Kirillov
 */
public interface ConfigurationPropertiesService {

    /**
     * Retrieves the configuration properties feed.
     * <p>
     * Values may be sanitized based on the current user's access level.
     * Users with {@link DefaultAuthority#CONFIG_PROPS_VALUES_READ} receive full (unsanitized) values.
     * Others receive sanitized values according to the configured sanitization rules.
     * </p>
     *
     * @return ConfigurationPropertiesFeed containing all configuration properties
     */
    ConfigurationPropertiesFeed getConfigProps();
}
