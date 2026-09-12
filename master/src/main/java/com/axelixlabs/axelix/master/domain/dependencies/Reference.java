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
package com.axelixlabs.axelix.master.domain.dependencies;

import java.net.URI;

/**
 * The upstream page that backs a catalog entry: the announcement, project page or migration guide a user can read to
 * verify the {@link SupportStatus} Axelix claims. Every entry carries one, so that no verdict shown in the UI is
 * unattributed.
 *
 * @param label the human-readable title of the page, e.g. {@code Sleuth to Micrometer Tracing migration}
 * @param url   the location of the page
 *
 * @author Mikhail Polivakha
 */
public record Reference(String label, URI url) {

    public static Reference of(String label, String url) {
        return new Reference(label, URI.create(url));
    }
}
