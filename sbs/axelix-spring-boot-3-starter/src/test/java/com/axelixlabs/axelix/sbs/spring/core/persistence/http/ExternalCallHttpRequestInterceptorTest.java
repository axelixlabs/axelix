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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import com.axelixlabs.axelix.common.domain.insights.TypeExternalCall;
import com.axelixlabs.axelix.sbs.spring.core.persistence.SimpleExternalCallRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionExecutionProfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ExternalCallHttpRequestInterceptor}.
 *
 * @author Sergey Cherkasov
 */
class ExternalCallHttpRequestInterceptorTest {

    private static final byte[] NO_BODY = new byte[0];

    private final TransactionAccessor transactionAccessor = new TransactionAccessor();

    @AfterEach
    void tearDown() {
        // The accessor keeps its state in a static ThreadLocal, so reset it to keep tests isolated.
        transactionAccessor.clearAll();
    }

    @Test
    void shouldRecordTheCallIntoTheActiveTransaction() throws IOException {
        // given.
        ExternalCallHttpRequestInterceptor subject = new ExternalCallHttpRequestInterceptor(transactionAccessor);
        transactionAccessor.recordNewTransactionStarted();
        MockClientHttpRequest request =
                new MockClientHttpRequest(HttpMethod.GET, URI.create("https://payments/charge"));

        // when.
        subject.intercept(request, NO_BODY, okExecution());
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();

        // then.
        assertThat(profile.getRecordedExternalCalls()).singleElement().satisfies(recorded -> {
            assertThat(recorded.getType()).isEqualTo(TypeExternalCall.HTTP_CLIENT);
            assertThat(recorded.getTarget()).isEqualTo("GET /charge");
            assertThat(recorded.getDurationMs()).isNotNegative();
        });
    }

    @Test
    void shouldReturnTheResponseProducedByTheExecution() throws IOException {
        // given.
        ExternalCallHttpRequestInterceptor subject = new ExternalCallHttpRequestInterceptor(transactionAccessor);
        transactionAccessor.recordNewTransactionStarted();
        MockClientHttpRequest request =
                new MockClientHttpRequest(HttpMethod.GET, URI.create("https://payments/charge"));
        ClientHttpResponse expectedResponse = new MockClientHttpResponse(NO_BODY, HttpStatus.OK);

        // when.
        ClientHttpResponse actualResponse = subject.intercept(request, NO_BODY, (req, body) -> expectedResponse);

        // then. The interceptor is transparent: it hands back exactly what the execution produced.
        assertThat(actualResponse).isSameAs(expectedResponse);
    }

    @Test
    void shouldStillRecordTheCallWhenTheExecutionFails() {
        // given. A blocking call that fails still held the transaction open, so it must be recorded.
        ExternalCallHttpRequestInterceptor subject = new ExternalCallHttpRequestInterceptor(transactionAccessor);
        transactionAccessor.recordNewTransactionStarted();
        MockClientHttpRequest request =
                new MockClientHttpRequest(HttpMethod.GET, URI.create("https://payments/charge"));
        ClientHttpRequestExecution failing = (req, body) -> {
            throw new IOException("connection reset");
        };

        // when.
        assertThatThrownBy(() -> subject.intercept(request, NO_BODY, failing))
                .isInstanceOf(IOException.class)
                .hasMessage("connection reset");
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();

        // then.
        assertThat(profile.getRecordedExternalCalls())
                .singleElement()
                .extracting(SimpleExternalCallRecord::getTarget)
                .isEqualTo("GET /charge");
    }

    private static ClientHttpRequestExecution okExecution() {
        return (request, body) -> new MockClientHttpResponse(NO_BODY, HttpStatus.OK);
    }
}
