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

import org.jspecify.annotations.NonNull;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.web.client.RestTemplate;

import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

import static com.axelixlabs.axelix.common.domain.insights.TypeExternalCall.REST_TEMPLATE;

/**
 * {@link RestTemplateCustomizer} that registers the {@link ExternalCallHttpRequestInterceptor} on every
 * {@link RestTemplate} assembled by the auto-configured {@link RestTemplateBuilder}.
 * <p>
 * This mirrors how Spring Boot instruments a {@link RestTemplate} itself — both its metrics and its observation
 * support attach through a {@link RestTemplateCustomizer} and nothing else. A {@link RestTemplate} that never goes
 * through the auto-configured {@link RestTemplateBuilder}, such as a plain {@code new RestTemplate()} bean, is
 * therefore deliberately not instrumented, exactly as it would carry no Spring Boot metrics.
 *
 * @author Sergey Cherkasov
 */
public class ExternalCallRestTemplateCustomizer implements RestTemplateCustomizer {

    private final TransactionAccessor transactionAccessor;

    public ExternalCallRestTemplateCustomizer(TransactionAccessor transactionAccessor) {
        this.transactionAccessor = transactionAccessor;
    }

    @Override
    public void customize(@NonNull RestTemplate restTemplate) {
        boolean alreadyInstrumented =
                restTemplate.getInterceptors().stream().anyMatch(ExternalCallHttpRequestInterceptor.class::isInstance);

        if (!alreadyInstrumented) {
            restTemplate
                    .getInterceptors()
                    .add(new ExternalCallHttpRequestInterceptor(transactionAccessor, REST_TEMPLATE));
        }
    }
}
