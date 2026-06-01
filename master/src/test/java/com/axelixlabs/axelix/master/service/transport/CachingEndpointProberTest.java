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
package com.axelixlabs.axelix.master.service.transport;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.axelixlabs.axelix.master.service.transport.CachingEndpointProber.CacheKey;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.common.domain.http.SingleValueQueryParameter;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CachingEndpointProber}.
 *
 * @author Mikhail Polivakha
 */
@ExtendWith(MockitoExtension.class)
class CachingEndpointProberTest {

    private static final InstanceId INSTANCE_A = InstanceId.of("instance-a");
    private static final InstanceId INSTANCE_B = InstanceId.of("instance-b");
    private static final String BASE_URL = "http://localhost:8080/actuator";
    private static final byte[] CACHED_BODY = new byte[] {1, 2, 3};
    private static final byte[] OTHER_BODY = new byte[] {4, 5, 6};

    @Mock
    private EndpointProber<byte[]> delegate;

    private CachingEndpointProber<byte[]> subject;

    @BeforeEach
    void setUp() {
        Cache<CacheKey, byte[]> cache = Caffeine.newBuilder().build();
        subject = new CachingEndpointProber<>(delegate, cache);
    }

    @Nested
    class InvokeByInstanceId {

        @Test
        void shouldReturnCachedValueOnSecondInvocationForSameInstance() {
            // given.
            when(delegate.invoke(eq(INSTANCE_A), same(NoHttpPayload.INSTANCE))).thenReturn(CACHED_BODY);

            // when.
            byte[] first = subject.invoke(INSTANCE_A, NoHttpPayload.INSTANCE);
            byte[] second = subject.invoke(INSTANCE_A, NoHttpPayload.INSTANCE);

            // then.
            assertThat(first).isSameAs(CACHED_BODY);
            assertThat(second).isSameAs(CACHED_BODY);
            verify(delegate, times(1)).invoke(eq(INSTANCE_A), same(NoHttpPayload.INSTANCE));
        }

        @Test
        void shouldInvokeDelegateSeparatelyForDifferentInstances() {
            // given.
            when(delegate.invoke(eq(INSTANCE_A), same(NoHttpPayload.INSTANCE))).thenReturn(CACHED_BODY);
            when(delegate.invoke(eq(INSTANCE_B), same(NoHttpPayload.INSTANCE))).thenReturn(OTHER_BODY);

            // when.
            byte[] resultA = subject.invoke(INSTANCE_A, NoHttpPayload.INSTANCE);
            byte[] resultB = subject.invoke(INSTANCE_B, NoHttpPayload.INSTANCE);

            // then.
            assertThat(resultA).isSameAs(CACHED_BODY);
            assertThat(resultB).isSameAs(OTHER_BODY);
            verify(delegate).invoke(eq(INSTANCE_A), same(NoHttpPayload.INSTANCE));
            verify(delegate).invoke(eq(INSTANCE_B), same(NoHttpPayload.INSTANCE));
        }

        @Test
        void shouldInvokeDelegateSeparatelyWhenHttpPayloadDiffers() {
            // given.
            HttpPayload firstPayload = NoHttpPayload.INSTANCE;
            HttpPayload secondPayload = new DefaultHttpPayload(
                    Collections.emptyList(),
                    List.of(new SingleValueQueryParameter("name", "value")),
                    Map.of(),
                    new byte[0]);
            when(delegate.invoke(eq(INSTANCE_A), same(firstPayload))).thenReturn(CACHED_BODY);
            when(delegate.invoke(eq(INSTANCE_A), same(secondPayload))).thenReturn(OTHER_BODY);

            // when.
            byte[] first = subject.invoke(INSTANCE_A, firstPayload);
            byte[] second = subject.invoke(INSTANCE_A, secondPayload);

            // then.
            assertThat(first).isSameAs(CACHED_BODY);
            assertThat(second).isSameAs(OTHER_BODY);
            verify(delegate).invoke(eq(INSTANCE_A), same(firstPayload));
            verify(delegate).invoke(eq(INSTANCE_A), same(secondPayload));
        }
    }

    @Test
    void shouldAlwaysDelegateWithoutCaching() {
        // given.
        when(delegate.invoke(eq(BASE_URL), same(NoHttpPayload.INSTANCE)))
            .thenReturn(CACHED_BODY)
            .thenReturn(OTHER_BODY);

        // when.
        byte[] first = subject.invoke(BASE_URL, NoHttpPayload.INSTANCE);
        byte[] second = subject.invoke(BASE_URL, NoHttpPayload.INSTANCE);

        // then.
        assertThat(first).isSameAs(CACHED_BODY);
        assertThat(second).isSameAs(OTHER_BODY);
        verify(delegate, times(2)).invoke(eq(BASE_URL), same(NoHttpPayload.INSTANCE));
        verifyNoMoreInteractions(delegate);
    }

    @ParameterizedTest
    @MethodSource("exceptionalOutcome")
    void shouldNotCacheExceptionalOutcome(Exception toBeThrown) throws Exception {
        // given.
        when(delegate.invoke(eq(INSTANCE_A), same(NoHttpPayload.INSTANCE)))
            .thenThrow(toBeThrown)
            .thenReturn(CACHED_BODY);

        // when.
        assertThatThrownBy(() -> subject.invoke(INSTANCE_A, NoHttpPayload.INSTANCE))
            .isInstanceOf(toBeThrown.getClass());
        byte[] result = subject.invoke(INSTANCE_A, NoHttpPayload.INSTANCE);

        // then.
        assertThat(result).isSameAs(CACHED_BODY);
        verify(delegate, times(2)).invoke(eq(INSTANCE_A), same(NoHttpPayload.INSTANCE));
    }

    @Test
    void shouldDelegateSupports() {
        // given.
        ActuatorEndpoint endpoint = ActuatorEndpoints.GET_BEANS;
        when(delegate.supports()).thenReturn(endpoint);

        // when.
        ActuatorEndpoint result = subject.supports();

        // then.
        assertThat(result).isSameAs(endpoint);
        verify(delegate).supports();
    }

    public static Stream<Arguments> exceptionalOutcome() {
        return Stream.of(
            of(new EndpointInvocationException("failure")),
            of(new InstanceNotFoundException()),
            of(new BadRequestException("failure"))
        );
    }
}
