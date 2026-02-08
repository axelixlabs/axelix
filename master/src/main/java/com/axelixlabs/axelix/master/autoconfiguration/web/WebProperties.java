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
package com.axelixlabs.axelix.master.autoconfiguration.web;

import org.springframework.core.io.Resource;

/**
 * The Web related properties that are custom to Axelix Master.
 *
 * @author Mikhail Polivakha
 */
@SuppressWarnings("NullAway.Init")
public class WebProperties {

    /**
     * the location (either inside or outside the classpath) that
     * represents the root directory for the SPA files (i.e. JavaScript
     * or CSS bundles, index.html)
     */
    private Resource location;

    public Resource getLocation() {
        return location;
    }

    public WebProperties setLocation(Resource location) {
        this.location = location;
        return this;
    }
}
