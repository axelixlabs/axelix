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
package com.axelixlabs.axelix.common.api.registration.insights.persistence;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.axelixlabs.axelix.common.domain.insights.TypeExternalCall;

/**
 * Aggregated timing of the blocking calls made to a single external endpoint from within a transactional
 * method, folded across every invocation of that method.
 *
 * @author Sergey Cherkasov
 */
public class ExternalCallInsight {

    private final TypeExternalCall type;
    private final String target;
    private final TransactionOverallStats stats;

    /**
     * Create a new ExternalCallInsight.
     *
     * @param type       the client that performed the call, e.g. {@link TypeExternalCall#REST_TEMPLATE} or
     *                   {@link TypeExternalCall#REST_CLIENT} for an HTTP call, {@link TypeExternalCall#KAFKA} for a
     *                   messaging one.
     * @param target     where the call went: the request method and url for an HTTP call, e.g.
     *                   {@code "GET https://payments/charge"}. The topic / queue / exchange for a messaging call.
     * @param stats      the min/max/avg call duration aggregated across every invocation of the transactional method.
     */
    @JsonCreator
    public ExternalCallInsight(
            @JsonProperty("type") TypeExternalCall type,
            @JsonProperty("target") String target,
            @JsonProperty("stats") TransactionOverallStats stats) {
        this.type = type;
        this.target = target;
        this.stats = stats;
    }

    public TypeExternalCall getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public TransactionOverallStats getStats() {
        return stats;
    }

    @Override
    public String toString() {
        return "ExternalCallInsight{" + "type='"
                + type + '\'' + ", target='"
                + target + '\'' + ", stats="
                + stats + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExternalCallInsight that = (ExternalCallInsight) o;
        return Objects.equals(type, that.type)
                && Objects.equals(target, that.target)
                && Objects.equals(stats, that.stats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, target, stats);
    }
}
