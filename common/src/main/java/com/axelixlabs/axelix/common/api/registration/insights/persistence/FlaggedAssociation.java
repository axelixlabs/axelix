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

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single association of a mapped entity in which Axelix detected a {@link AssociationProblem}.
 *
 * @author Mikhail Polivakha
 */
public class FlaggedAssociation {

    /**
     * The association in which the problem was detected.
     */
    private final Association association;

    /**
     * A human-readable rendering of the offending mapping (e.g.
     * {@code @ManyToOne(fetch = FetchType.EAGER)}).
     */
    private final String mapping;

    /**
     * The category of the detected problem.
     */
    private final Set<AssociationProblem> problems;

    @JsonCreator
    public FlaggedAssociation(
            @JsonProperty("association") Association association,
            @JsonProperty("mapping") String mapping,
            @JsonProperty("problems") Set<AssociationProblem> problems) {
        this.association = association;
        this.mapping = mapping;
        this.problems = problems;
    }

    public Association getAssociation() {
        return association;
    }

    public String getMapping() {
        return mapping;
    }

    public Set<AssociationProblem> getProblems() {
        return problems;
    }
}
