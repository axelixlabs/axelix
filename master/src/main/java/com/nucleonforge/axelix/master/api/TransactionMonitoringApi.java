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
                        "propagationTestHelper.PropagationTestHelper.testNestedRequiresNew",
                        List.of(125L, 110L, 98L, 135L, 115L, 125L, 110L, 98L, 135L, 115L, 125L, 110L, 98L, 135L, 115L)),
                new TransactionMonitoringFeed(
                        "propagationTestService.PropagationTestService.testRequired",
                        List.of(25L, 11L, 288L, 13L, 15L, 25L, 11L, 288L, 13L, 15L)),
                new TransactionMonitoringFeed(
                        "propagationTestService.PropagationTestService.testSupports",
                        List.of(225L, 280L, 198L, 235L, 275L, 225L, 280L, 198L, 235L, 275L)));
    }
}
