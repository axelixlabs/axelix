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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CachingAxelixVersionDiscoverer}.
 *
 * @author Aleksei Ermakov
 */
@ExtendWith(MockitoExtension.class)
class CachingAxelixVersionDiscovererTest {

    @Mock
    private AxelixVersionDiscoverer delegate;

    @Nested
    class GetVersion {

        @Test
        void returnsVersionFromDelegate() { // GH-773
            // given.
            when(delegate.getVersion()).thenReturn("1.0.0");
            CachingAxelixVersionDiscoverer subject = new CachingAxelixVersionDiscoverer(delegate);

            // when.
            String version = subject.getVersion();

            // then.
            assertThat(version).isEqualTo("1.0.0");
        }

        @Test
        void callsDelegateOnlyOnce_onMultipleInvocations() { // GH-773
            // given.
            when(delegate.getVersion()).thenReturn("1.0.0");
            CachingAxelixVersionDiscoverer subject = new CachingAxelixVersionDiscoverer(delegate);

            // when.
            subject.getVersion();
            subject.getVersion();
            subject.getVersion();

            // then.
            verify(delegate, times(1)).getVersion();
        }
    }
}
