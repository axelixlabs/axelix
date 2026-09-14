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
package com.axelixlabs.axelix.master.service.ecosystem.projects;

import java.util.Optional;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;

/**
 * The catalog of the {@link SoftwareProject Software Projects} Axelix Master is aware about. Essentially acts as the
 * central accessor to all known Software Projects.
 *
 * @author Mikhail Polivakha
 */
public interface SoftwareProjectsCatalog {

    /**
     * Resolves the {@link SoftwareProject} by the provided {@link Library}.
     *
     * @param library the library to infer the {@link SoftwareProject} for.
     *
     * @return the curated Software Project, or {@link Optional#empty()} when Axelix Master is not capable to
     *         infer the Software Project for the provided {@link Library}.
     */
    Optional<SoftwareProject> resolve(Library library);
}
