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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import com.axelixlabs.axelix.common.api.registration.SelfRegistrationMetadata;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;

/**
 * Default implementation of {@link SelfRegistrationHttpClient}
 *
 * @since 05.02.2026
 * @author Nikita Kirillov
 */
public class DefaultSelfRegistrationHttpClient implements SelfRegistrationHttpClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DefaultSelfRegistrationHttpClient() {
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public HttpResponse<Void> sendRequest(@NonNull SelfRegistrationMetadata selfRegistrationMetadata, String url)
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
            for (var header : httpPayload.headers()) {
                builder.header(header.name(), header.valueAsString());
            }
        }
        return builder.build();
    }
}
