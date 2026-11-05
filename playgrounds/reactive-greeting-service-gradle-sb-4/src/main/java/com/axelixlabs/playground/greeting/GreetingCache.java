package com.axelixlabs.playground.greeting;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/** Cache-backed operations used to exercise Axelix cache management on WebFlux. */
@Service
public class GreetingCache {

    private final AtomicLong loadCount = new AtomicLong();

    @Cacheable("greetings")
    public String get(String key) {
        return "Hello from cache, " + key + "! (load " + loadCount.incrementAndGet() + ")";
    }

    public long getLoadCount() {
        return loadCount.get();
    }
}
