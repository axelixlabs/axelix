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
package com.axelixlabs.axelix.gradle.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.gradle.api.GradleException;
import org.gradle.api.Task;

/**
 * Configuration-cache compatibility helpers that keep the plugin loadable on Gradle 5.
 *
 * @author Mikhail Polivakha
 */
public final class ConfigurationCacheSupport {

    private ConfigurationCacheSupport() {}

    public static void markNotCompatible(Task task, String reason) {
        try {
            Method method = Task.class.getMethod("notCompatibleWithConfigurationCache", String.class);
            method.invoke(task, reason);
        } catch (NoSuchMethodException ignored) {
            // Gradle versions before 7.4 do not expose the configuration-cache opt-out API.
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GradleException("Failed to configure configuration-cache compatibility", e);
        }
    }
}
