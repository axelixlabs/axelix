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

/**
 * A neutral category of a problem detected in a JPA association mapping while Axelix scanned the
 * entities of an instance. The categories are not ranked against each other, they merely describe
 * the kind of mapping smell that was found.
 *
 * @author Mikhail Polivakha
 */
public enum AssociationProblem {

    /**
     * The association is fetched eagerly (either declared {@code FetchType.EAGER} or defaulting to
     * it, as {@code @ManyToOne} / {@code @OneToOne} do), so it is loaded on every parent load
     * whether or not the data is used.
     */
    EAGER_FETCHING,

    /**
     * A {@code @ManyToMany} association backed by a {@link java.util.List}, which forces the
     * persistence provider to clear and re-insert the whole join table on any change.
     */
    LIST_BACKED_MANY_TO_MANY,

    /**
     * A to-many association ({@code @OneToMany} or {@code @ManyToMany}) whose cascade set contains
     * {@code CascadeType.ALL} or {@code CascadeType.REMOVE}, so removing the parent cascades a delete to
     * every child.
     */
    CASCADE_REMOVE_OR_ALL,

    /**
     * A {@code @OneToMany} declared without {@code mappedBy}, i.e. unidirectional, which requires an
     * extra join table or per-row updates instead of a plain foreign key.
     */
    UNIDIRECTIONAL_ONE_TO_MANY
}
