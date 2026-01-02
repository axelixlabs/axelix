/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.common.domain;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

/**
 * {@link AxelixVersionDiscoverer} that is based on the java properties file.
 *
 * @author Mikhail Polivakha
 */
// TODO: introduce the caching decorator for this class.
public class PropertiesAxelixVersionDiscoverer implements AxelixVersionDiscoverer {

    private static final String VERSION_KEY = "version";

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
        try {
            URL resource = ClassLoader.getSystemClassLoader().getResource(propertiesFilePath);

            checkResourceFound(resource);

            File proeprtiesFile = new File(resource.toURI());
            Properties properties = new Properties();

            properties.load(new FileReader(proeprtiesFile));

            String version = properties.getProperty(VERSION_KEY);

            checkVersionFound(version);

            return version;

        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void checkVersionFound(@Nullable String version) {
        if (version == null) {
            throw new IllegalStateException(
                    "Axelix properties file under '%s' does not contain version mapping (value for key '%s')"
                            .formatted(propertiesFilePath, VERSION_KEY));
        }
    }

    private void checkResourceFound(@Nullable URL resource) {
        if (resource == null) {
            throw new IllegalStateException(
                    "The provided path to axelix properties file '%s' cannot be found".formatted(propertiesFilePath));
        }
    }
}
