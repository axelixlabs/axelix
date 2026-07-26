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

import feign.Client;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ExternalCallFeignCapability}.
 *
 * @author Sergey Cherkasov
 */
class ExternalCallFeignCapabilityTest {

    private final ExternalCallFeignCapability subject = new ExternalCallFeignCapability(new TransactionAccessor());

    @Test
    void shouldInstrumentTheClient() {
        // given.
        Client client = new Client.Default(null, null);

        // when.
        Client result = subject.enrich(client);

        // then.
        assertThat(result).isInstanceOf(ExternalCallFeignClient.class);
    }

    @Test
    void shouldNotInstrumentTheSameClientTwice() {
        // given.
        Client instrumented = subject.enrich(new Client.Default(null, null));

        // when.
        Client result = subject.enrich(instrumented);

        // then. A second decoration would make every call be recorded twice.
        assertThat(result).isSameAs(instrumented);
    }
}
