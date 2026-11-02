package com.axelixlabs.playground.greeting;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The regular and the reactive {@code @Scheduled} tasks. The executions of the reactive ones end when their
 * publishers complete.
 */
@Component
@EnableScheduling
public class GreetingScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(GreetingScheduledTasks.class);

    @Scheduled(fixedRate = 30_000)
    public void logGreetingsStats() {
        log.info("Plain scheduled task executed");
    }

    @Scheduled(fixedDelay = 45_000)
    public Mono<Void> refreshGreetings() {
        return Mono.delay(Duration.ofSeconds(1))
                .doOnNext(ignored -> log.info("Mono scheduled task executed"))
                .then();
    }

    @Scheduled(fixedRate = 60_000)
    public Mono<Void> syncGreetings() {
        return Mono.delay(Duration.ofMillis(500))
                .then(Mono.error(new IllegalStateException("Greetings source is unavailable")));
    }

    @Scheduled(cron = "0 */2 * * * *")
    public Flux<Long> cleanupGreetings() {
        return Flux.interval(Duration.ofMillis(200))
                .take(3)
                .doOnComplete(() -> log.info("Flux scheduled task executed"));
    }
}
