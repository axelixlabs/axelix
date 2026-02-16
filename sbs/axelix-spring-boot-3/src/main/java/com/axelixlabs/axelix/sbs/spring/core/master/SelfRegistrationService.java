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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.axelixlabs.axelix.common.api.registration.SelfRegistrationMetadata;
import com.axelixlabs.axelix.common.domain.http.HttpHeader;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.sbs.spring.core.config.SelfRegistrationConfigurationProperties;

/**
 * Self-registration service that automatically registers with master.
 *
 * @since 05.02.2026
 * @author Nikita Kirillov
 */
public class SelfRegistrationService implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(SelfRegistrationService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SelfRegistrationConfigurationProperties properties;
    private final SelfRegistrationMetadataAssembler selfRegistrationMetadataAssembler;
    private final ScheduledExecutorService executor;

    public SelfRegistrationService(
            SelfRegistrationConfigurationProperties properties,
            SelfRegistrationMetadataAssembler selfRegistrationMetadataAssembler) {

        this.httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.selfRegistrationMetadataAssembler = selfRegistrationMetadataAssembler;

        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        register();

        executor.scheduleAtFixedRate(
                this::register,
                properties.getHeartbeatInterval().getSeconds(),
                properties.getHeartbeatInterval().getSeconds(),
                TimeUnit.SECONDS);
    }

    private void register() {
        SelfRegistrationMetadata selfRegistrationMetadata = selfRegistrationMetadataAssembler.assemble();

        try {
            HttpResponse<Void> response = sendRequest(selfRegistrationMetadata, properties.getMasterUrl());

            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                log.trace("Heartbeat successful. Master URL: {}", properties.getMasterUrl());
            } else {
                log.info("Master heartbeat failed, HTTP status: {}\"", statusCode);
            }
        } catch (IOException | InterruptedException e) {
            log.info("Error sending registration request or heartbeat to master: {}", e.getMessage());
        }
    }

    private HttpResponse<Void> sendRequest(@NonNull SelfRegistrationMetadata selfRegistrationMetadata, String url)
            throws IOException, InterruptedException {
        HttpPayload payload = HttpPayload.json(serializeToJson(selfRegistrationMetadata));
        HttpRequest request = buildHttpRequest(url, payload);
        return httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    private byte[] serializeToJson(SelfRegistrationMetadata metadata) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(metadata);
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
    public void destroy() {
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
