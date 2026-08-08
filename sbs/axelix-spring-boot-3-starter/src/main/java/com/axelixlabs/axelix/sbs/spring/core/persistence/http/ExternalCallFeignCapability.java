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

import feign.Capability;
import feign.Client;

import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

/**
 * {@link Capability} that decorates the {@link Client} of every Feign client with the
 * {@link ExternalCallFeignClient}, so that the calls performed while a transaction is open are recorded as
 * external calls.
 *
 * @author Sergey Cherkasov
 */
public class ExternalCallFeignCapability implements Capability {

    private final TransactionAccessor transactionAccessor;

    public ExternalCallFeignCapability(TransactionAccessor transactionAccessor) {
        this.transactionAccessor = transactionAccessor;
    }

    @Override
    public Client enrich(Client client) {
        return client instanceof ExternalCallFeignClient
                ? client
                : new ExternalCallFeignClient(client, transactionAccessor);
    }
}
