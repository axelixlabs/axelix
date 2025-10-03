package com.nucleonforge.axile.master.service.convert;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.caches.CacheDispatcherClearResult;
import com.nucleonforge.axile.master.api.response.caches.CacheDispatcherClearResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CacheDispatcherClearResultConverter}
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
class CacheDispatcherClearResultConverterTest {

    private final CacheDispatcherClearResultConverter subject = new CacheDispatcherClearResultConverter();

    @Test
    void testConvertHappyPath() {
        CacheDispatcherClearResult cacheDispatcherClearResult = new CacheDispatcherClearResult(true);

        // when.
        CacheDispatcherClearResponse response = subject.convertInternal(cacheDispatcherClearResult);

        // then.
        assertThat(response.cleared()).isTrue();
    }
}
