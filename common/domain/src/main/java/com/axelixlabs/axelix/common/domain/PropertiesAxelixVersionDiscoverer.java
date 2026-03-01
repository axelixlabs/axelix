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
package com.axelixlabs.axelix.common.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

/**
 * {@link AxelixVersionDiscoverer} that is based on the java properties file.
 *
 * @author Mikhail Polivakha
 */
public class PropertiesAxelixVersionDiscoverer implements AxelixVersionDiscoverer {

    private static final String VERSION_KEY = "version";

    private static final ClassLoader CLASS_LOADER = PropertiesAxelixVersionDiscoverer.class.getClassLoader();

    private final String propertiesFilePath;

    /**
     * @param propertiesFilePath path towards the axelix properties file. The path is expected
     *                           to be relative to the current classpath.
     */
    public PropertiesAxelixVersionDiscoverer(String propertiesFilePath) {
        this.propertiesFilePath = propertiesFilePath;
    }

    @Override
    public String getVersion() throws IllegalStateException {
        try (InputStream propertiesFileStream = CLASS_LOADER.getResourceAsStream(propertiesFilePath)) {

            checkResourceFound(propertiesFileStream);

            Properties properties = new Properties();

            properties.load(propertiesFileStream);

            String version = properties.getProperty(VERSION_KEY);

            checkVersionFound(version);

            return version;

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void checkVersionFound(@Nullable String version) {
        if (version == null) {
            throw new IllegalStateException(String.format(
                    "Axelix properties file under '%s' does not contain version mapping (value for key '%s')",
                    propertiesFilePath, VERSION_KEY));
        }
    }

    private void checkResourceFound(@Nullable InputStream resource) {
        if (resource == null) {
            throw new IllegalStateException(String.format(
                    "The provided path to axelix properties file '%s' cannot be found", propertiesFilePath));
        }
    }
}
