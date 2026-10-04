package com.axelixlabs.playground.gateway;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * Tiny stateless gateway surface. No persistence of any kind.
 */
@RestController
public class GatewayController {

    private final RestClient upstream;

    public GatewayController(
            RestClient.Builder restClientBuilder, @Value("${gateway.upstream-base-url}") String upstreamBaseUrl) {
        // The upstream host is fixed by configuration (not taken from the request), so this is a
        // plain reverse-forward to a single backend, not an open proxy.
        this.upstream = restClientBuilder.baseUrl(upstreamBaseUrl).build();
    }

    @GetMapping("/")
    public Map<String, Object> info() {
        return Map.of(
                "service", "api-gateway-gradle-sb-4",
                "hasDatabase", false,
                "note", "Stateless edge service — boots with no relational DB and no spring-tx on the classpath");
    }

    /**
     * Forwards to the single, operator-configured upstream ({@code gateway.upstream-base-url}).
     * Demonstrates the outbound-call path a gateway exercises; not required for startup.
     */
    @GetMapping("/api/upstream")
    public ResponseEntity<String> upstream() {
        return upstream.get().retrieve().toEntity(String.class);
    }
}
