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
package com.nucleonforge.axelix.master.api.request.scheduled;

/**
 * Represents a request to modify the interval of a scheduled task.
 *
 * @param taskId   the identifier of the scheduled task to modify. Must not be {@code null}.
 * @param interval the new interval to be assigned (in milliseconds).
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public record ScheduledTaskIntervalModifyRequest(String taskId, Long interval) {}
