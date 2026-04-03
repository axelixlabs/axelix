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

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Cache for all {@link ConfigurationPropertiesBean} instances.
 *
 * @author Nikita Kirillov
 */
public class ConfigurationPropertiesBeansCache {

    private final ConfigurableApplicationContext applicationContext;

    @Nullable
    private volatile Map<String, ConfigurationPropertiesBean> cachedResult;

    public ConfigurationPropertiesBeansCache(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Nullable
    public ConfigurationPropertiesBean findConfigurationPropertiesBeanByPropertyName(String propertyName) {
        return getConfigurationPropertiesBeans().values().stream()
                .filter(bean -> propertyName.startsWith(bean.getAnnotation().prefix()))
                .findFirst()
                .orElse(null);
    }

    // Yeah, here we trigger the initialization of all ConfigurationPropertiesBean.
    // We assume that this endpoint is called only after the user opens the
    // ConfigurationProperties page on the UI, which means the standard
    // /actuator/configprops or /actuator/axelix-configprops has already been invoked
    // and all lazy beans are already initialized anyway.
    // This behavior is standard for Spring Boot 2, 3, and 4 as of 01.04.2026.
    private Map<String, ConfigurationPropertiesBean> getConfigurationPropertiesBeans() {
        if (cachedResult == null) {
            synchronized (this) {
                if (cachedResult == null) {
                    cachedResult = new HashMap<>();
                    ApplicationContext ctx = applicationContext;
                    while (ctx != null) {
                        cachedResult.putAll(ConfigurationPropertiesBean.getAll(ctx));
                        ctx = ctx.getParent();
                    }
                }
            }
        }
        return cachedResult;
    }
}
