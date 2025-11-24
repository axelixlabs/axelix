package com.nucleonforge.axile.master.service.convert.caches;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.caches.SingleCache;
import com.nucleonforge.axile.master.api.response.caches.CacheProfileResponse;
import com.nucleonforge.axile.master.service.convert.response.caches.SingleCacheConverter;

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
        SingleCache cache = new SingleCache("cities", "java.util.concurrent.ConcurrentHashMap", "cacheManager");

        // when
        CacheProfileResponse response = subject.convertInternal(cache);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("cities");
        assertThat(response.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(response.cacheManager()).isEqualTo("cacheManager");
    }
}
