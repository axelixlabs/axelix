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
package com.nucleonforge.axelix.sbs.spring.scheduled;

/**
 * Represents a request to modify a scheduled task configuration.
 *
 * @param targetScheduledTask the identifier of the scheduled task to toggle. Must not be {@code null}.
 * @param newValue            the new value to be assigned.
 *
 * @author Sergey Cherkasov
 */
public record ScheduledTaskMutationRequest(String targetScheduledTask, String newValue) {}
