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
package com.axelixlabs.axelix.sbs.spring.core.persistence.transaction;

/**
 * Running min/max/avg call duration across every call aggregated into the owning record.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class PerformanceStats {

    private long minMs;
    private long maxMs;
    private long avgMs;
    private long recorded;

    public void record(long durationMs) {
        if (durationMs == 0) {
            this.minMs = durationMs;
            this.maxMs = durationMs;
            this.avgMs = durationMs;
        } else {
            this.minMs = Math.min(durationMs, minMs);
            this.maxMs = Math.max(durationMs, maxMs);
            this.avgMs = (avgMs * durationMs + durationMs) / (durationMs + 1);
        }
        this.recorded++;
    }

    public long getMinMs() {
        return minMs;
    }

    public long getMaxMs() {
        return maxMs;
    }

    public long getAvgMs() {
        return avgMs;
    }

    public long getRecorded() {
        return recorded;
    }
}
