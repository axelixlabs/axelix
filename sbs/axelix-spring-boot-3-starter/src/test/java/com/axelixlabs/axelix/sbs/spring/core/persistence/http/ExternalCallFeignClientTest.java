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
import java.util.Map;

import feign.Client;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Request.Options;
import feign.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.domain.insights.TypeExternalCall;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionExecutionProfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ExternalCallFeignClient}.
 *
 * @author Sergey Cherkasov
 */
class ExternalCallFeignClientTest {

    private final TransactionAccessor transactionAccessor = new TransactionAccessor();

    @AfterEach
    void tearDown() {
        // The accessor keeps its state in a static ThreadLocal, so reset it to keep tests isolated.
        transactionAccessor.clearAll();
    }

    @Test
    void shouldRecordTheCallIntoTheActiveTransaction() throws IOException {
        // given.
        ExternalCallFeignClient subject = new ExternalCallFeignClient(okDelegate(), transactionAccessor);
        transactionAccessor.recordNewTransactionStarted();

        // when.
        subject.execute(request("http://payments/charge?id=1"), new Options());
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();

        // then. The host of a Feign url is the name of the called service, so it is a part of the target.
        assertThat(profile.getRecordedExternalCalls()).singleElement().satisfies(recorded -> {
            assertThat(recorded.getType()).isEqualTo(TypeExternalCall.HTTP_CLIENT);
            assertThat(recorded.getTarget()).isEqualTo("GET payments/charge");
            assertThat(recorded.getDurationMs()).isNotNegative();
        });
    }

    @Test
    void shouldSanitizeTheTargetStrippingCredentialsQueryAndFragment() throws IOException {
        // given.
        ExternalCallFeignClient subject = new ExternalCallFeignClient(okDelegate(), transactionAccessor);
        transactionAccessor.recordNewTransactionStarted();

        // when.
        subject.execute(request("http://user:pass@payments/charge?id=1#frag"), new Options());
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();

        // then. Credentials, query and fragment must never leak into the recorded target.
        assertThat(profile.getRecordedExternalCalls())
                .singleElement()
                .satisfies(recorded -> assertThat(recorded.getTarget()).isEqualTo("GET payments/charge"));
    }

    @Test
    void shouldRecordOnlyThePathForARelativeUrl() throws IOException {
        // given. A relative url has no host, so the fallback must still not persist the raw url with its query.
        ExternalCallFeignClient subject = new ExternalCallFeignClient(okDelegate(), transactionAccessor);
        transactionAccessor.recordNewTransactionStarted();

        // when.
        subject.execute(request("/charge?id=1"), new Options());
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();

        // then.
        assertThat(profile.getRecordedExternalCalls())
                .singleElement()
                .satisfies(recorded -> assertThat(recorded.getTarget()).isEqualTo("GET /charge"));
    }

    @Test
    void shouldReturnTheResponseProducedByTheDelegate() throws IOException {
        // given.
        Response expectedResponse = response(request("http://payments/charge"));
        ExternalCallFeignClient subject =
                new ExternalCallFeignClient((request, options) -> expectedResponse, transactionAccessor);
        transactionAccessor.recordNewTransactionStarted();

        // when.
        Response actualResponse = subject.execute(request("http://payments/charge"), new Options());

        // then. The client is transparent: it hands back exactly what the delegate produced.
        assertThat(actualResponse).isSameAs(expectedResponse);
    }

    @Test
    void shouldStillRecordTheCallWhenTheDelegateFails() {
        // given. A blocking call that fails still held the transaction open, so it must be recorded.
        Client failing = (request, options) -> {
            throw new IOException("connection reset");
        };
        ExternalCallFeignClient subject = new ExternalCallFeignClient(failing, transactionAccessor);
        transactionAccessor.recordNewTransactionStarted();

        // when.
        assertThatThrownBy(() -> subject.execute(request("http://payments/charge"), new Options()))
                .isInstanceOf(IOException.class)
                .hasMessage("connection reset");

        // then.
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();
        assertThat(profile.getRecordedExternalCalls())
                .singleElement()
                .satisfies(recorded -> assertThat(recorded.getTarget()).isEqualTo("GET payments/charge"));
    }

    @Test
    void shouldRecordNothingWhenThereIsNoActiveTransaction() throws IOException {
        // given. No transaction was started, so the call blocks nothing.
        ExternalCallFeignClient subject = new ExternalCallFeignClient(okDelegate(), transactionAccessor);

        // when.
        subject.execute(request("http://payments/charge"), new Options());
        transactionAccessor.recordNewTransactionStarted();
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();

        // then.
        assertThat(profile.getRecordedExternalCalls()).isEmpty();
    }

    private static Client okDelegate() {
        return (request, options) -> response(request);
    }

    private static Request request(String url) {
        return Request.create(HttpMethod.GET, url, Map.of(), null, null, null);
    }

    private static Response response(Request request) {
        return Response.builder().status(200).request(request).build();
    }
}
