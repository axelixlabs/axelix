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
package com.axelixlabs.axelix.sbs.spring.core.transactions;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

/**
 * A data container that holds the state of an active transaction trace context.
 *
 * @param txSpan the root distributed tracing span for the current transaction
 * @param scope  the active scope allocation bounding the span context to the executing thread
 *
 * @author Nikita Kirillov
 */
public record TransactionTraceContext(Span txSpan, Tracer.SpanInScope scope) {}
