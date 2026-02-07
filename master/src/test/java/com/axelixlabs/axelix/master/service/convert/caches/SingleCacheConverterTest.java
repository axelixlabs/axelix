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
package com.axelixlabs.axelix.master.service.convert.caches;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.caches.SingleCache;
import com.axelixlabs.axelix.master.api.external.response.caches.CacheProfileResponse;
import com.axelixlabs.axelix.master.service.convert.response.caches.SingleCacheConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SingleCacheConverter}
 *
 * @author Sergey Cherkasov
 */
public class SingleCacheConverterTest {
    private final SingleCacheConverter subject = new SingleCacheConverter();

    @Test
    void testConvertHappyPath() {
        SingleCache cache =
                new SingleCache("cities", "java.util.concurrent.ConcurrentHashMap", "cacheManager", 1L, 2L, 3L, true);

        // when
        CacheProfileResponse response = subject.convertInternal(cache);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("cities");
        assertThat(response.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(response.cacheManager()).isEqualTo("cacheManager");
    }
}
