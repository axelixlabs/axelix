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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.NonNull;

import com.axelixlabs.axelix.common.api.registration.SelfRegistrationMetadata;
import com.axelixlabs.axelix.common.domain.http.HttpHeader;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.sbs.spring.core.config.SelfRegistrationConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.log.Logger;

/**
 * Self-registration service that automatically registers with master.
 *
 * @since 05.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class SelfRegistrationService implements Closeable {

    private final HttpClient httpClient;
    private final JsonSerializationFunction serializationFunction;
    private final SelfRegistrationConfigurationProperties properties;
    private final SelfRegistrationMetadataAssembler selfRegistrationMetadataAssembler;
    private final ScheduledExecutorService executor;
    private final Logger logger;

    public SelfRegistrationService(
            Logger logger,
            JsonSerializationFunction serializationFunction,
            SelfRegistrationConfigurationProperties properties,
            SelfRegistrationMetadataAssembler selfRegistrationMetadataAssembler) {

        this.logger = logger;
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        this.properties = properties;
        this.serializationFunction = serializationFunction;
        this.selfRegistrationMetadataAssembler = selfRegistrationMetadataAssembler;

        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }

    public void scheduleSelfRegistration() {
        executor.scheduleAtFixedRate(
                this::register, 0L, properties.getHeartbeatInterval().getSeconds(), TimeUnit.SECONDS);
    }

    private void register() {
        SelfRegistrationMetadata selfRegistrationMetadata = selfRegistrationMetadataAssembler.assemble();

        try {
            HttpResponse<Void> response = sendRequest(selfRegistrationMetadata, properties.getMasterUrl());

            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                logger.trace("Heartbeat successful. Master URL: {}", properties.getMasterUrl());
            } else {
                logger.info("Master heartbeat failed, HTTP status: {}\"", statusCode);
            }
        } catch (IOException | InterruptedException e) {
            logger.info("Error sending registration request or heartbeat to master: {}", e.getMessage());
        }
    }

    private HttpResponse<Void> sendRequest(@NonNull SelfRegistrationMetadata selfRegistrationMetadata, String url)
            throws IOException, InterruptedException {
        HttpPayload payload = HttpPayload.json(
                serializationFunction.serialize(selfRegistrationMetadata).getBytes(StandardCharsets.UTF_8));
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
