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

import org.jspecify.annotations.NonNull;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.axelixlabs.axelix.common.domain.insights.TypeExternalCall;
import com.axelixlabs.axelix.sbs.spring.core.persistence.SimpleExternalCallRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

/**
 * {@link ClientHttpRequestInterceptor} that records every synchronous HTTP call performed while a transaction
 * is open, so that the blocking calls holding the transaction become visible. It serves any client built on
 * {@link ClientHttpRequestInterceptor} — both {@code RestTemplate} and {@code RestClient} — recording every such
 * call as a {@link TypeExternalCall#HTTP_CLIENT} one.
 * <p>
 * Whether the call actually belongs to a transaction is decided by the {@link TransactionAccessor}: calls
 * made outside of any transaction are silently dropped by it.
 *
 * @author Sergey Cherkasov
 */
class ExternalCallHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final TransactionAccessor transactionAccessor;

    ExternalCallHttpRequestInterceptor(TransactionAccessor transactionAccessor) {
        this.transactionAccessor = transactionAccessor;
    }

    @Override
    public @NonNull ClientHttpResponse intercept(
            @NonNull HttpRequest request, byte @NonNull [] body, @NonNull ClientHttpRequestExecution execution)
            throws IOException {

        long startNanos = System.nanoTime();

        try {
            return execution.execute(request, body);
        } finally {
            long duration = System.nanoTime() - startNanos;
            String target = request.getMethod().name() + " " + request.getURI().getPath();
            transactionAccessor.recordExternalCall(
                    new SimpleExternalCallRecord(TypeExternalCall.HTTP_CLIENT, target, duration / 1_000_000));
        }
    }
}
