/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.domain.http.HttpHeader;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.sbs.spring.core.config.HeartBeatConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.log.Logger;

/**
 * Self-registration service that automatically registers with master.
 *
 * @since 05.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Andrey Kotov
 */
public class HeartBeatService implements Closeable {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String REGISTRATION_PATH = "/service/register";
    private static final String HEARTBEAT_PATH = "/service/heartbeat/v2";
    private static final PasswordlessUser TECH_USER =
            new PasswordlessUser("AXELIX.STARTER", "AXELIX.STARTER", Set.of(DefaultRole.MANAGED_SERVICE));

    private final HttpClient httpClient;
    private final JsonSerializationFunction serializationFunction;
    private final HeartBeatConfigurationProperties properties;
    private final HeartBeatMetadataAssembler heartBeatMetadataAssembler;
    private final ScheduledExecutorService executor;
    private final Logger logger;
    private final JwtEncoderService jwtEncoderService;

    @Nullable
    private volatile String heartbeatUrl;

    @SuppressWarnings("NullAway.Init")
    private volatile String currentToken;

    private volatile boolean registered;

    public HeartBeatService(
            Logger logger,
            JsonSerializationFunction serializationFunction,
            HeartBeatConfigurationProperties properties,
            HeartBeatMetadataAssembler heartBeatMetadataAssembler,
            JwtEncoderService jwtEncoderService) {

        this.logger = logger;
        this.jwtEncoderService = jwtEncoderService;
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        this.properties = properties;
        this.serializationFunction = serializationFunction;
        this.heartBeatMetadataAssembler = heartBeatMetadataAssembler;

        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "axelix-heartbeat");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void scheduleHeartBeat() {
        currentToken = jwtEncoderService.generateToken(TECH_USER);
        heartbeatUrl = resolveHeartbeatUrl(properties.getMasterUrl());
        executor.scheduleAtFixedRate(
                this::heartbeat, 0L, properties.getHeartbeatInterval().getSeconds(), TimeUnit.SECONDS);
    }

    private void heartbeat() {
        try {
            String heartbeatUrl = this.heartbeatUrl;

            if (registered && heartbeatUrl != null) {
                int statusCode = sendRequest(heartBeatMetadataAssembler.assembleHeartBeatV2(), heartbeatUrl)
                        .statusCode();

                if (is2xxSuccessful(statusCode)) {
                    logger.trace("Heartbeat successful. Master URL: {}", heartbeatUrl);
                } else if (isUnauthorized(statusCode)) {
                    logger.debug("Master heartbeat failed. Token expired. Re-generating token");
                    currentToken = jwtEncoderService.generateToken(TECH_USER);
                } else if (isConflict(statusCode)) {
                    logger.info("Master does not know this instance. Registering again");
                    registered = false;
                    register();
                } else {
                    logger.info("Master heartbeat failed, HTTP status: {}\"", statusCode);
                }
            } else {
                register();
            }
        } catch (IOException | InterruptedException e) {
            logger.info("Error sending registration request or heartbeat to master: {}", e.getMessage());
        }
    }

    private void register() throws IOException, InterruptedException {
        int statusCode = sendRequest(
                        heartBeatMetadataAssembler.assembleRegistrationRequest(), properties.getMasterUrl())
                .statusCode();

        if (is2xxSuccessful(statusCode)) {
            logger.trace("Registration successful. Master URL: {}", properties.getMasterUrl());
            registered = true;
        } else if (isUnauthorized(statusCode)) {
            logger.debug("Master registration failed. Token expired. Re-generating token");
            currentToken = jwtEncoderService.generateToken(TECH_USER);
        } else {
            logger.info("Master registration failed, HTTP status: {}\"", statusCode);
        }
    }

    @Nullable
    private static String resolveHeartbeatUrl(String masterUrl) {
        if (!masterUrl.endsWith(REGISTRATION_PATH)) {
            return null;
        }
        return masterUrl.substring(0, masterUrl.length() - REGISTRATION_PATH.length()) + HEARTBEAT_PATH;
    }

    private static boolean is2xxSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private static boolean isUnauthorized(int statusCode) {
        return statusCode == 401;
    }

    /**
     * The conflict means that the master does not know this instance (e.g. the master has been restarted),
     * so the starter has to register itself again.
     */
    private static boolean isConflict(int statusCode) {
        return statusCode == 409;
    }

    private HttpResponse<Void> sendRequest(Object body, String url) throws IOException, InterruptedException {
        HttpPayload payload =
                HttpPayload.json(serializationFunction.serialize(body).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = buildHttpRequest(url, payload);
        return httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    private HttpRequest buildHttpRequest(String url, HttpPayload httpPayload) {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(httpPayload.requestBody());

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(2))
                .method(HttpMethod.POST.name(), bodyPublisher)
                .uri(URI.create(url));

        if (httpPayload.hasHeaders()) {
            for (HttpHeader header : httpPayload.headers()) {
                builder.header(header.name(), header.valueAsString());
            }
        }

        builder.header(AUTHORIZATION_HEADER, AuthenticationSchemes.BEARER.prefix() + currentToken);

        return builder.build();
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
