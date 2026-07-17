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
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ExternalCallRestTemplateCustomizer}.
 *
 * @author Sergey Cherkasov
 */
class ExternalCallRestTemplateCustomizerTest {

    private final ExternalCallRestTemplateCustomizer subject =
            new ExternalCallRestTemplateCustomizer(new TransactionAccessor());

    @Test
    void shouldInstrumentRestTemplate() {
        // given.
        RestTemplate restTemplate = new RestTemplate();

        // when.
        subject.customize(restTemplate);

        // then.
        assertThat(restTemplate.getInterceptors())
                .hasSize(1)
                .hasOnlyElementsOfType(ExternalCallHttpRequestInterceptor.class);
    }

    @Test
    void shouldKeepInterceptorsTheRestTemplateAlreadyCarried() {
        // given.
        ClientHttpRequestInterceptor existing = (request, body, execution) -> execution.execute(request, body);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(new ArrayList<>(List.of(existing)));

        // when.
        subject.customize(restTemplate);

        // then.
        assertThat(restTemplate.getInterceptors())
                .hasSize(2)
                .contains(existing)
                .hasAtLeastOneElementOfType(ExternalCallHttpRequestInterceptor.class);
    }

    @Test
    void shouldNotInstrumentTheSameRestTemplateTwice() {
        // given. A RestTemplateBuilder is reusable, so nothing stops an application from having us customize the
        // same template twice.
        RestTemplate restTemplate = new RestTemplate();
        subject.customize(restTemplate);

        // when.
        subject.customize(restTemplate);

        // then. A second interceptor would make every outgoing call be recorded twice.
        assertThat(restTemplate.getInterceptors())
                .hasSize(1)
                .hasOnlyElementsOfType(ExternalCallHttpRequestInterceptor.class);
    }

    @Test
    void shouldNotReorderTheInterceptorsTheApplicationRegistered() {
        // given. RestTemplateBuilder keeps the order the application registered them in. The @Order(1) is what
        // makes this test able to fail at all: AnnotationAwareOrderComparator scores an interceptor without any
        // order metadata as Ordered.LOWEST_PRECEDENCE and the sort is stable, so two unordered interceptors
        // would come out of a sort untouched and the bug would slip through.
        UnorderedInterceptor unordered = new UnorderedInterceptor();
        OrderedInterceptor ordered = new OrderedInterceptor();

        RestTemplate restTemplate = new RestTemplateBuilder()
                .additionalInterceptors(unordered, ordered)
                .build();

        // when.
        subject.customize(restTemplate);

        // then. The application's own order must survive, with ours appended last.
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();

        assertThat(interceptors).hasSize(3);
        assertThat(interceptors.subList(0, 2)).containsExactly(unordered, ordered);
        assertThat(interceptors.get(2)).isInstanceOf(ExternalCallHttpRequestInterceptor.class);
    }

    @Order(1)
    private static class OrderedInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                throws IOException {
            return execution.execute(request, body);
        }
    }

    private static class UnorderedInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                throws IOException {
            return execution.execute(request, body);
        }
    }
}
