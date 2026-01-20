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

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axelix.master.api.response.TransactionMonitoringFeed;
import com.nucleonforge.axelix.master.api.response.TransactionMonitoringFeed.TransactionExecution;

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
    public List<TransactionMonitoringFeed> getTransactionFeed() {
        return Arrays.asList(
                new TransactionMonitoringFeed(
                        "PropagationTestHelper",
                        "testNestedRequiresNew",
                        List.of(
                                new TransactionExecution(125L, "1737374615"),
                                new TransactionExecution(110L, "1737374625"),
                                new TransactionExecution(98L, "1737374635"),
                                new TransactionExecution(135L, "1737374645"),
                                new TransactionExecution(115L, "1737374655"),
                                new TransactionExecution(125L, "1737374665"),
                                new TransactionExecution(110L, "1737374675"),
                                new TransactionExecution(98L, "1737374685"),
                                new TransactionExecution(135L, "1737374695"),
                                new TransactionExecution(115L, "1737374705"),
                                new TransactionExecution(125L, "1737374715"),
                                new TransactionExecution(110L, "1737374725"),
                                new TransactionExecution(98L, "1737374735"),
                                new TransactionExecution(135L, "1737374745"),
                                new TransactionExecution(115L, "1737374755"))),
                new TransactionMonitoringFeed(
                        "PropagationTestService",
                        "testRequired",
                        List.of(
                                new TransactionExecution(25L, "1737374765"),
                                new TransactionExecution(11L, "1737374775"),
                                new TransactionExecution(288L, "1737374785"),
                                new TransactionExecution(13L, "1737374795"),
                                new TransactionExecution(15L, "1737374805"),
                                new TransactionExecution(25L, "1737374815"),
                                new TransactionExecution(11L, "1737374825"),
                                new TransactionExecution(288L, "1737374835"),
                                new TransactionExecution(13L, "1737374845"),
                                new TransactionExecution(15L, "1737374855"))),
                new TransactionMonitoringFeed(
                        "PropagationTestService",
                        "testSupports",
                        List.of(
                                new TransactionExecution(225L, "1737374865"),
                                new TransactionExecution(280L, "1737374875"),
                                new TransactionExecution(198L, "1737374885"),
                                new TransactionExecution(235L, "1737374895"),
                                new TransactionExecution(275L, "1737374905"),
                                new TransactionExecution(225L, "1737374915"),
                                new TransactionExecution(280L, "1737374925"),
                                new TransactionExecution(198L, "1737374935"),
                                new TransactionExecution(235L, "1737374945"),
                                new TransactionExecution(275L, "1737374955"))));
    }
}
