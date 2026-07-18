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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link KubernetesDetector}.
 *
 * @author Ilya Naumov
 */
@ExtendWith(SystemStubsExtension.class)
class KubernetesDetectorTest {
    @SystemStub
    private EnvironmentVariables environmentVariables;

    private final KubernetesDetector subject = new KubernetesDetector();

    @Test // GH-1219
    void returnTrue_whenServiceHostVariableExists() {
        // given.
        environmentVariables.set("KUBERNETES_SERVICE_HOST", "10.96.0.1");

        // when.
        boolean result = subject.hasKubernetesMarker();

        // then.
        assertThat(result).isTrue();
    }

    @Test // GH-1219
    void returnFalse_whenServiceHostVariableDoesNotExist() {
        // when.
        boolean result = subject.hasKubernetesMarker();

        // then.
        assertThat(result).isFalse();
    }
}
