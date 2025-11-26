package com.nucleonforge.axile.common.domain.http;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Tests for the {@link HttpUrl}.
 *
 * @author Mikhail Polivakha
 */
class HttpUrlTest {

    @MethodSource(value = "args")
    @ParameterizedTest
    @DisplayName("Should expand the HTTP Url")
    void testExpandingUrl(
            String url, String eventualUrl, Map<String, String> valuesMap, QueryParameter<?>[] queryParameters) {
        HttpUrl httpUrl = new HttpUrl(url);

        String expand = httpUrl.expand(valuesMap, Arrays.stream(queryParameters).toList());

        assertThat(expand).isEqualTo(eventualUrl);
    }

    static Stream<Arguments> args() {
        return Stream.of(
                of(
                        "/health/{component}",
                        "/health/database?key1=value1&key2=value2",
                        Map.of("component", "database"),
                        new QueryParameter[] {
                            new SingleValueQueryParameter("key1", "value1"),
                            new SingleValueQueryParameter("key2", "value2")
                        }),
                of("/health", "/health?key1=value1", Map.of("component", "database"), new QueryParameter[] {
                    new SingleValueQueryParameter("key1", "value1")
                }),
                of(
                        "/health/{component}/sub-component",
                        "/health/redis/sub-component?key2=value2",
                        Map.of("component", "redis"),
                        new QueryParameter[] {new SingleValueQueryParameter("key2", "value2")}),
                of("/health", "/health?key2=value2", Map.of(), new QueryParameter[] {
                    new SingleValueQueryParameter("key2", "value2")
                }),
                of("/health/data", "/health/data", Map.of(), new QueryParameter[] {}),
                of("/{cache.name}", "/my-cache", Map.of("cache.name", "my-cache"), new QueryParameter[] {}),
                of(
                        "/{cacheManager}/{cacheName}/clear",
                        "/myManager/myCache/clear",
                        Map.of("cacheManager", "myManager", "cacheName", "myCache"),
                        new QueryParameter[] {}),
                of(
                        "/{first}/{second}/{first}/{third}",
                        "/A/B/A/C",
                        Map.of("first", "A", "second", "B", "third", "C"),
                        new QueryParameter[] {}));
    }
}
