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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Inspects the PID 1 process to determine whether the application is running
 * inside an isolated environment.
 *
 * @author Ilya Naumov
 */
public class FirstPidInspector {
    private static final List<String> INITIAL_PROCESSES = List.of("init", "systemd");

    /**
     * Checks whether PID 1 is not an initial process (init or systemd),
     * which typically indicates the application is running in a container.
     *
     * @return {@code true} if PID 1 is not an initial process
     */
    public boolean isFirstPidNotInitialProcess() {
        String firstProcess;
        try {
            firstProcess = Files.readString(Path.of("/proc/1/comm")).trim();
        } catch (IOException e) {
            return false;
        }

        return INITIAL_PROCESSES.stream().noneMatch(process -> Objects.equals(process, firstProcess));
    }
}
