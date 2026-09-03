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
package com.axelixlabs.axelix.sbs.spring.core.persistence.http;

import java.io.IOException;
import java.net.URI;

import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;

import com.axelixlabs.axelix.common.domain.insights.TypeExternalCall;
import com.axelixlabs.axelix.sbs.spring.core.persistence.SimpleExternalCallRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

/**
 * {@link Client} that records every call performed by a Feign client while a transaction is open, so that
 * the blocking calls holding the transaction become visible. Every such call is recorded as a
 * {@link TypeExternalCall#HTTP_CLIENT} one, just like a {@code RestTemplate} call is.
 *
 * @author Sergey Cherkasov
 */
class ExternalCallFeignClient implements Client {

    private final Client delegate;
    private final TransactionAccessor transactionAccessor;

    ExternalCallFeignClient(Client delegate, TransactionAccessor transactionAccessor) {
        this.delegate = delegate;
        this.transactionAccessor = transactionAccessor;
    }

    @Override
    public Response execute(Request request, Options options) throws IOException {
        long startNanos = System.nanoTime();

        try {
            return delegate.execute(request, options);
        } finally {
            long duration = System.nanoTime() - startNanos;
            try {
                transactionAccessor.recordExternalCall(new SimpleExternalCallRecord(
                        TypeExternalCall.HTTP_CLIENT, resolveTarget(request), duration / 1_000_000));
            } catch (RuntimeException ignored) {
                // Instrumentation must never replace the Feign result: swallow any recording failure.
            }
        }
    }

    private static String resolveTarget(Request request) {
        String method = request.httpMethod().name();

        try {
            URI uri = URI.create(request.url());
            String host = uri.getHost();
            String path = uri.getPath() == null ? "" : uri.getPath();

            if (host != null) {
                return method + " " + host + path;
            }
            return path.isEmpty() ? method : method + " " + path;
        } catch (IllegalArgumentException e) {
            return method;
        }
    }
}
