package com.nucleonforge.axile.master.autoconfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Very simple and minimal logging interceptor for HTTP requests into K8S control plane.
 *
 * @author Mikhail Polivakha
 */
public class ControlPlainHttpRequestsLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ControlPlainHttpRequestsLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        log.trace(
                """
            Sending an HTTP request to K8S control plain
            Method: {}
            Path: {}
            Body: {}
            """,
                request.getMethod(),
                request.getURI(),
                new String(body, StandardCharsets.UTF_8));

        return execution.execute(request, body);
    }
}
