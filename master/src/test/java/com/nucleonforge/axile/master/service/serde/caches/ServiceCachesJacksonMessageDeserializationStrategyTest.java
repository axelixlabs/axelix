package com.nucleonforge.axile.master.service.serde.caches;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.caches.ServiceCaches;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ServiceCachesJacksonMessageDeserializationStrategy}.
 *
 * @author Sergey Cherkasov
 */
public class ServiceCachesJacksonMessageDeserializationStrategyTest {
    private final ServiceCachesJacksonMessageDeserializationStrategy subject =
            new ServiceCachesJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeServiceCaches() {
        // language=json
        String response =
                """
           {
          "cacheManagers" : {
            "anotherCacheManager" : {
              "caches" : {
                "countries" : {
                  "target" : "java.util.concurrent.ConcurrentHashMap"
                }
              }
            },
            "cacheManager" : {
              "caches" : {
                "cities" : {
                  "target" : "java.util.concurrent.ConcurrentHashMap"
                },
                "countries" : {
                  "target" : "java.util.concurrent.ConcurrentHashMap"
                }
              }
            }
          }
          }
        """;

        // when.
        ServiceCaches caches = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // ServiceCaches -> CacheManagers
        Map<String, ServiceCaches.CacheManagers> cacheManagers = caches.cacheManagers();
        assertThat(cacheManagers).hasSize(2).containsKeys("anotherCacheManager", "cacheManager");

        // CacheManagers -> "anotherCacheManager"
        ServiceCaches.CacheManagers another = cacheManagers.get("anotherCacheManager");
        assertThat(another.caches()).hasSize(1).containsKey("countries");

        // "anotherCacheManager" -> Caches -> "countries"
        assertThat(another.caches().get("countries").target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");

        // CacheManagers -> "cacheManager"
        ServiceCaches.CacheManagers main = cacheManagers.get("cacheManager");
        assertThat(main.caches()).hasSize(2).containsKeys("cities", "countries");

        // "cacheManager" -> Caches -> "countries"
        assertThat(main.caches().get("cities").target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        // "cacheManager" -> Caches -> "cities"
        assertThat(main.caches().get("countries").target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
    }
}
