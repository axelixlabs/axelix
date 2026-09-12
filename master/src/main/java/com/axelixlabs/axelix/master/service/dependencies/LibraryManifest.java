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
package com.axelixlabs.axelix.master.service.dependencies;

import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.dependencies.ArtifactCoordinates;
import com.axelixlabs.axelix.master.domain.dependencies.Ecosystem;
import com.axelixlabs.axelix.master.domain.dependencies.KnownLibrary;
import com.axelixlabs.axelix.master.domain.dependencies.LibraryId;
import com.axelixlabs.axelix.master.domain.dependencies.Reference;
import com.axelixlabs.axelix.master.domain.dependencies.Succession;
import com.axelixlabs.axelix.master.domain.dependencies.SupportStatus;

/**
 * The on-disk shape of one curated manifest file, and nothing more. It exists so that the domain records stay free of
 * Jackson annotations and of the string forms the file format uses, matching how {@code common.api} DTOs are kept
 * separate from {@code common.domain} value objects elsewhere in the codebase.
 * <p>
 * The {@link Ecosystem} is declared once per file rather than on every entry. One file per ecosystem is what keeps
 * the grouping consistent, and it removes a field that would otherwise be repeated identically on every entry.
 *
 * @param ecosystem the area every library in this file belongs to
 * @param libraries the curated entries
 *
 * @author Mikhail Polivakha
 */
record LibraryManifest(Ecosystem ecosystem, List<Entry> libraries) {

    List<KnownLibrary> toLibraries() {
        return libraries.stream().map(entry -> entry.toLibrary(ecosystem)).toList();
    }

    /**
     * @param id          the stable identifier, in lower kebab-case
     * @param name        the name the project is known by
     * @param status      what the authors are still doing with the project
     * @param summary     what happened to the project, in prose
     * @param coordinates every artifact of the project, in the {@code groupId:artifactId} notation
     * @param succession  where to go instead, omitted for an active project
     * @param reference   the upstream page backing the status
     */
    record Entry(
            String id,
            String name,
            SupportStatus status,
            String summary,
            List<String> coordinates,
            @Nullable SuccessionEntry succession,
            ReferenceEntry reference) {

        KnownLibrary toLibrary(Ecosystem ecosystem) {
            return new KnownLibrary(
                    LibraryId.of(id),
                    name,
                    ecosystem,
                    status,
                    summary,
                    coordinates.stream().map(ArtifactCoordinates::parse).collect(Collectors.toUnmodifiableSet()),
                    succession == null ? null : new Succession(succession.kind(), succession.value()),
                    Reference.of(reference.label(), reference.url()));
        }
    }

    record SuccessionEntry(Succession.Kind kind, String value) {}

    record ReferenceEntry(String label, String url) {}
}
