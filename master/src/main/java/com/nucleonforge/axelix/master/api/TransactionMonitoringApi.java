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
package com.nucleonforge.axelix.master.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axelix.master.api.response.TransactionMonitoringFeed;
import com.nucleonforge.axelix.master.api.response.TransactionMonitoringFeed.TransactionExecution;
import com.nucleonforge.axelix.master.api.response.TransactionMonitoringFeed.TransactionalEntrypoint;

/**
 * The API for Transaction Monitoring.
 *
 * @since 20.01.2026
 * @author Nikita Kirillov
 */
@RestController
@RequestMapping(path = ApiPaths.TransactionMonitoringApi.MAIN)
public class TransactionMonitoringApi {

    @GetMapping(path = ApiPaths.TransactionMonitoringApi.INSTANCE_ID)
    public TransactionMonitoringFeed getTransactionFeed() {
        return new TransactionMonitoringFeed(List.of(
                new TransactionalEntrypoint(
                        "PropagationTestHelper",
                        "testNestedRequiresNew",
                        List.of(
                                new TransactionExecution(125L, 1737374615L),
                                new TransactionExecution(110L, 1737374625L),
                                new TransactionExecution(98L, 1737374635L),
                                new TransactionExecution(135L, 1737374645L),
                                new TransactionExecution(115L, 1737374655L),
                                new TransactionExecution(125L, 1737374665L),
                                new TransactionExecution(110L, 1737374675L),
                                new TransactionExecution(98L, 1737374685L),
                                new TransactionExecution(135L, 1737374695L),
                                new TransactionExecution(115L, 1737374705L),
                                new TransactionExecution(125L, 1737374715L),
                                new TransactionExecution(110L, 1737374725L),
                                new TransactionExecution(98L, 1737374735L),
                                new TransactionExecution(135L, 1737374745L),
                                new TransactionExecution(115L, 1737374755L))),
                new TransactionalEntrypoint(
                        "PropagationTestService",
                        "testRequired",
                        List.of(
                                new TransactionExecution(25L, 1737374765L),
                                new TransactionExecution(11L, 1737374775L),
                                new TransactionExecution(288L, 1737374785L),
                                new TransactionExecution(13L, 1737374795L),
                                new TransactionExecution(15L, 1737374805L),
                                new TransactionExecution(25L, 1737374815L),
                                new TransactionExecution(11L, 1737374825L),
                                new TransactionExecution(288L, 1737374835L),
                                new TransactionExecution(13L, 1737374845L),
                                new TransactionExecution(15L, 1737374855L))),
                new TransactionalEntrypoint(
                        "PropagationTestService",
                        "testSupports",
                        List.of(
                                new TransactionExecution(225L, 1737374865L),
                                new TransactionExecution(280L, 1737374875L),
                                new TransactionExecution(198L, 1737374885L),
                                new TransactionExecution(235L, 1737374895L),
                                new TransactionExecution(275L, 1737374905L),
                                new TransactionExecution(225L, 1737374915L),
                                new TransactionExecution(280L, 1737374925L),
                                new TransactionExecution(198L, 1737374935L),
                                new TransactionExecution(235L, 1737374945L),
                                new TransactionExecution(275L, 1737374955L)))));
    }
}
