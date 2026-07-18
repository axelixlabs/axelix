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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Detects whether the application is running inside a Docker container.
 *
 * @author Ilya Naumov
 */
public class DockerDetector {
    private static final String DOCKER_ENVIRONMENT_PATH = "/.dockerenv";

    /**
     * Checks whether the Docker environment marker file exists,
     * which indicates a Docker container environment.
     *
     * @return {@code true} if the Docker environment marker file is present
     */
    public boolean hasDockerEnvironmentFile() {
        return Files.exists(Paths.get(DOCKER_ENVIRONMENT_PATH));
    }
}
