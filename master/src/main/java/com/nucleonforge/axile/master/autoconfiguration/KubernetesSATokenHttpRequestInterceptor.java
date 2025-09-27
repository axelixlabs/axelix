package com.nucleonforge.axile.master.autoconfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.jsonwebtoken.lang.Assert;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * The {@link ClientHttpRequestInterceptor} used for attaching the access token header to the request
 * for the K8S control plane.
 *
 * @author Mikhail Polivakha
 */
public class KubernetesSATokenHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(KubernetesSATokenHttpRequestInterceptor.class);

    /**
     * Access token to be used for authenticating in the K8S control place API server.
     */
    private final @NonNull String accessToken;

    public KubernetesSATokenHttpRequestInterceptor(String tokenLocation) {
        Path path = Paths.get(tokenLocation);
        this.accessToken = "Bearer %s".formatted(readToken(path));
        log.info("Access token for control plane requests is successfully resolved from {}", tokenLocation);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        log.info("Enriched request {}:{} with access token {}", request.getMethod(), request.getURI(), accessToken);

        HttpHeaders headers = request.getHeaders();

        headers.compute(HttpHeaders.AUTHORIZATION, (k, v) -> List.of(accessToken));

        return execution.execute(request, body);
    }

    private static String readToken(Path path) {
        Assert.state(
                Files.isRegularFile(path),
                """
            The specified path '%s' to the ServiceAccount token that is used
            for authentication with control place API server is not correct.
            The file either does not exits or does not represent a regular file.
            """
                        .formatted(path.toString()));

        try {
            return Files.readString(path, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unable to read the ServiceAccount access token '%s' used for accessing the control plane"
                            .formatted(path.toString()),
                    e);
        }
    }
}
