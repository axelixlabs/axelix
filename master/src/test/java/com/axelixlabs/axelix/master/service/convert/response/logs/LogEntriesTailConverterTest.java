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
package com.axelixlabs.axelix.master.service.convert.response.logs;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.contract.logs.LogEntriesTail;
import com.axelixlabs.axelix.master.contract.logs.LogEntry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LogEntriesTailConverter}.
 *
 * @author Mikhail Polivakha
 */
class LogEntriesTailConverterTest {

    private final LogEntriesTailConverter subject = new LogEntriesTailConverter();

    @Test
    void shouldRenderEachEntryAsTheConsoleLikeLine() {
        // given.
        LogEntriesTail tail = new LogEntriesTail()
                .entries(List.of(
                        new LogEntry()
                                .timestamp(1760264130123L)
                                .level("INFO")
                                .logger("com.example.OrderService")
                                .thread("main")
                                .message("Order created"),
                        new LogEntry()
                                .timestamp(1760264131456L)
                                .level("ERROR")
                                .logger("com.example.PaymentService")
                                .thread("http-nio-8080-exec-1")
                                .message("Payment failed")
                                .stackTrace(
                                        "java.lang.IllegalStateException: boom\n\tat com.example.Pay.pay(Pay.java:42)\n"),
                        new LogEntry()
                                .timestamp(1760264132000L)
                                .level("WARN")
                                .logger("com.example.OrderService")
                                .thread("main")
                                .message("Retrying")));

        // when.
        String result = subject.convertInternal(tail);

        // then.
        assertThat(result).isEqualTo("""
                2025-10-12T10:15:30.123Z INFO [main] com.example.OrderService - Order created
                2025-10-12T10:15:31.456Z ERROR [http-nio-8080-exec-1] com.example.PaymentService - Payment failed
                java.lang.IllegalStateException: boom
                \tat com.example.Pay.pay(Pay.java:42)
                2025-10-12T10:15:32Z WARN [main] com.example.OrderService - Retrying
                """);
    }

    @Test
    void shouldRenderNothingWhenThereAreNoEntries() {
        assertThat(subject.convertInternal(new LogEntriesTail().entries(List.of())))
                .isEmpty();
    }
}
