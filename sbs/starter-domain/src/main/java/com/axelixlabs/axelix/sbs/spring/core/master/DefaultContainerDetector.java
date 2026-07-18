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

/**
 * Default implementation of {@link ContainerEnvironmentDetector}.
 *
 * @author Ilya Naumov
 */
public class DefaultContainerDetector implements ContainerEnvironmentDetector {
    private final KubernetesDetector kubernetesDetector;
    private final DockerDetector dockerDetector;
    private final FirstPidInspector firstPidInspector;

    public DefaultContainerDetector(
            KubernetesDetector kubernetesDetector, DockerDetector dockerDetector, FirstPidInspector firstPidInspector) {
        this.kubernetesDetector = kubernetesDetector;
        this.dockerDetector = dockerDetector;
        this.firstPidInspector = firstPidInspector;
    }

    /**
     * Determines whether the current application is running inside a container.
     *
     * <p>The detection evaluates the following signals in order:
     * <ol>
     *   <li>Presence of the Kubernetes environment variable.</li>
     *   <li>Presence of the Docker marker file.</li>
     *   <li>Whether PID 1 is a well-known system init process.</li>
     * </ol>
     */
    @Override
    public boolean isRunningInContainer() {
        if (kubernetesDetector.hasKubernetesServiceHostVariable()) {
            return true;
        }

        if (dockerDetector.hasDockerEnvironmentFile()) {
            return true;
        }

        return firstPidInspector.isFirstPidNotInitialProcess();
    }
}
