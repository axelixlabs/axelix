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
package com.axelixlabs.axelix.common.domain;

/**
 * Interface for discovering the current version of Axelix software distribution.
 *
 * @author Mikhail Polivakha
 */
public interface AxelixVersionDiscoverer {

    /**
     * @return the version of the Axelix distribution. Never {@code null}.
     * @throws IllegalStateException in case version cannot be determined or
     *         there is an error in the process.
     */
    String getVersion() throws IllegalStateException;
}
