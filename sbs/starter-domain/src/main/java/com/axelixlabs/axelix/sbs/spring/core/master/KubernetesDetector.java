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
 * Detects whether the application is running in a Kubernetes environment.
 *
 * @author Ilya Naumov
 */
public class KubernetesDetector {
    private static final String K8S_SERVICE_HOST_VARIABLE = "KUBERNETES_SERVICE_HOST";

    /**
     * Checks whether the Kubernetes service host environment variable is set,
     * which indicates a Kubernetes environment.
     *
     * @return {@code true} if the Kubernetes service host variable is present
     */
    public boolean hasKubernetesMarker() {
        return System.getenv(K8S_SERVICE_HOST_VARIABLE) != null;
    }
}
