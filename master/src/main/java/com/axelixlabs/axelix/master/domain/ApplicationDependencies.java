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
package com.axelixlabs.axelix.master.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

/**
 * The runtime dependency SBOM of the given application.
 *
 * @param applicationDependenciesId the id of the application.
 * @param sbom                      the CycloneDX SBOM.
 * @param reportedAt                when the SBOM was reported.
 *
 * @author Nikita Kirillov
 */
@Table("application_dependencies")
public record ApplicationDependencies(
        @Id @Embedded.Empty ApplicationDependenciesId applicationDependenciesId,
        String sbom,
        @Column("reported_at") Instant reportedAt) {

    /**
     * The composite key ID.
     *
     * @param groupId    the group id of the application.
     * @param artifactId the artifact id of the application.
     */
    // TODO:
    //  We should use the ApplicationId here, but we cannot now do that since we need Spring Data JDBC 4.1
    //  that supports embedded fields in the composite keys
    public record ApplicationDependenciesId(String groupId, String artifactId) {

        public static ApplicationDependenciesId of(ApplicationId applicationId) {
            return new ApplicationDependenciesId(applicationId.groupId(), applicationId.artifactId());
        }
    }
}
