package com.axelixlabs.playground.greeting;

import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class GreetingController {

    private final GreetingCache greetingCache;

    public GreetingController(GreetingCache greetingCache) {
        this.greetingCache = greetingCache;
    }

    @GetMapping(value = "/greetings/{name}", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> greet(@PathVariable String name) {
        return Mono.just("Hello, " + name + "!");
    }

    @GetMapping("/ticks")
    public Flux<Long> ticks() {
        return Flux.interval(Duration.ofMillis(100)).take(5);
    }

    @GetMapping(value = "/cache/{key}", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> cachedGreeting(@PathVariable String key) {
        return Mono.just(greetingCache.get(key));
    }

    @GetMapping(value = "/cache/load-count", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> cacheLoadCount() {
        return Mono.just(Long.toString(greetingCache.getLoadCount()));
    }
}
