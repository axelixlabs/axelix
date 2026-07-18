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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link DefaultLayeredImageStateProvider}.
 *
 * @author Ilya Naumov
 */
@ExtendWith(MockitoExtension.class)
class DefaultLayeredImageStateProviderTest {
    @Mock
    private ContainerEnvironmentDetector containerEnvironmentDetector;

    @Mock
    private LibraryLocationProvider libraryLocationProvider;

    @InjectMocks
    private DefaultLayeredImageStateProvider subject;

    @Test // GH-1219
    void returnTrue_whenRunningInContainerAndLibraryLocationHasFileProtocol() {
        // given.
        when(containerEnvironmentDetector.isRunningInContainer()).thenReturn(true);
        when(libraryLocationProvider.hasFileProtocol()).thenReturn(true);

        // when.
        boolean result = subject.isLayeredImageEnabled();

        // then.
        assertThat(result).isTrue();
    }

    @Test // GH-1219
    void returnFalse_whenRunningInContainerButLibraryLocationDoesNotHaveFileProtocol() {
        // given.
        when(containerEnvironmentDetector.isRunningInContainer()).thenReturn(true);
        when(libraryLocationProvider.hasFileProtocol()).thenReturn(false);

        // when.
        boolean result = subject.isLayeredImageEnabled();

        // then.
        assertThat(result).isFalse();
    }

    @Test // GH-1219
    void returnFalse_whenNotRunningInContainer() {
        // given.
        when(containerEnvironmentDetector.isRunningInContainer()).thenReturn(false);

        // when.
        boolean result = subject.isLayeredImageEnabled();

        // then.
        assertThat(result).isFalse();
    }
}
