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
import java.net.http.HttpResponse;

import org.jspecify.annotations.NonNull;

import com.axelixlabs.axelix.common.api.registration.SelfRegistrationMetadata;

/**
 * Contract interface for sending registration and heartbeat requests to the master.
 *
 * @since 05.02.2026
 * @author Nikita Kirillov
 */
public interface SelfRegistrationHttpClient {

    /**
     * Sends a registration or heartbeat request to the master node.
     * <p>
     * The method serializes the provided metadata to JSON and sends it as a POST request
     * to the specified URL.
     * </p>
     *
     * @param selfRegistrationMetadata metadata containing information about this instance
     *                                 that needs to be sent to the master for registration.
     * @param url                      the master URL to send the request to.
     * @return {@link HttpResponse} with a {@code Void} body since the response body is discarded.
     * @throws IOException          if an I/O error occurs when sending or receiving the request.
     * @throws InterruptedException if the operation is interrupted.
     */
    HttpResponse<Void> sendRequest(@NonNull SelfRegistrationMetadata selfRegistrationMetadata, String url)
            throws IOException, InterruptedException;
}
