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

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link DefaultContainerDetector}.
 *
 * @author Ilya Naumov
 */
@ExtendWith(MockitoExtension.class)
class DefaultContainerEnvironmentDetectorTest {
    @Mock
    private KubernetesDetector kubernetesDetector;

    @Mock
    private DockerDetector dockerDetector;

    @InjectMocks
    private DefaultContainerDetector subject;

    @ParameterizedTest // GH-1219
    @MethodSource("anyContainerMarks")
    void returnTrue_whenHasAnyMarker(Boolean hasKubernetesMarker, Boolean hasDockerMarker) {
        // given.
        when(kubernetesDetector.hasKubernetesMarker()).thenReturn(hasKubernetesMarker);
        lenient().when(dockerDetector.hasDockerMarker()).thenReturn(hasDockerMarker);

        // when.
        boolean result = subject.isRunningInContainer();

        // then.
        assertThat(result).isTrue();
    }

    @Test // GH-1219
    void returnFalse_whenNoContainerMarkers() {
        // given.
        doReturn(false).when(kubernetesDetector).hasKubernetesMarker();
        doReturn(false).when(dockerDetector).hasDockerMarker();

        // when.
        boolean result = subject.isRunningInContainer();

        // then.
        assertThat(result).isFalse();
    }

    private static Stream<Arguments> anyContainerMarks() {
        return Stream.of(Arguments.of(true, false), Arguments.of(false, true));
    }
}
