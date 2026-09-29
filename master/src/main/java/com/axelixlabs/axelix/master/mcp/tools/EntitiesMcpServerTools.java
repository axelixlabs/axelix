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
package com.axelixlabs.axelix.master.mcp.tools;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpTool.McpAnnotations;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.axelixlabs.axelix.master.contract.metadata.JpaEntities;
import com.axelixlabs.axelix.master.contract.metadata.MappedEntity;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

/**
 * MCP Tools that expose the entities profile (the JPA entities Axelix mapped in an application together with the
 * association-mapping problems detected inside each of them) of a given application.
 *
 * @since 26.07.2026
 * @author Mikhail Polivakha
 */
@Service
public class EntitiesMcpServerTools {

    private final ObjectMapper objectMapper;
    private final DatabaseHistoricalApplicationSnapshotService applicationSnapshotService;

    public EntitiesMcpServerTools(
            ObjectMapper objectMapper, DatabaseHistoricalApplicationSnapshotService applicationSnapshotService) {
        this.objectMapper = objectMapper;
        this.applicationSnapshotService = applicationSnapshotService;
    }

    @McpTool(
            name = McpEndpoints.ENTITIES_PROFILE_TOOL_NAME,
            title = "Entities Profile",
            description = """
            Get the entities profile of an application: the JPA entities Axelix detected in the app (their name and the
            table they map to) together with the association-mapping problems detected inside each of them, such as
            eager fetching, list-backed @ManyToMany, cascade REMOVE/ALL and unidirectional @OneToMany.

            The application is identified by its 'groupId' and 'artifactId' (the G and A of the GAV coordinate of
            the service artifact). You can typically find them in the build file of the project, e.g. in pom.xml or
            build.gradle or build.gradle.kts. Both must be provided together to identify the application.

            You may optionally narrow the result down by providing the 'entityName' (e.g. 'Order') to get the
            profile of that single entity only. When it is omitted, the profile of every mapped entity of the
            application is returned.
        """,
            annotations =
                    @McpAnnotations(
                            title = "JPA entities mapped in an application and the problems detected in their"
                                    + " associations",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getApplicationEntitiesProfile(
            @McpToolParam(required = false, description = """
                    The groupId of the application (the G of the GAV coordinate). Must be provided together with
                    'artifactId' to identify the application.
                    """) @Nullable String groupId,
            @McpToolParam(required = false, description = """
                    The artifactId of the application (the A of the GAV coordinate). Must be provided together with
                    'groupId' to identify the application.
                    """) @Nullable String artifactId,
            @McpToolParam(required = false, description = """
                    The name of the entity to inspect (e.g. 'Order'). When provided, only the profile of this single
                    entity is returned; when omitted, the profile of every mapped entity of the application is
                    returned.
                    """) @Nullable String entityName) {

        if (!StringUtils.hasText(groupId) || !StringUtils.hasText(artifactId)) {
            return "Provide both 'groupId' and 'artifactId' to identify the application whose entities profile you"
                    + " want.";
        }

        HistoricalApplicationSnapshot snapshot =
                applicationSnapshotService.getCurrentRecord(ApplicationId.of(groupId, artifactId));

        if (snapshot == null) {
            return "No application found with groupId '%s' and artifactId '%s'.".formatted(groupId, artifactId);
        }

        JpaEntities entitiesMap = snapshot.insights().persistenceInsights().getEntitiesMap();

        if (entitiesMap == null) {
            return "No entities map has been recorded for application '%s:%s'.".formatted(groupId, artifactId);
        }

        // A single entity is requested: return the profile of that one entity.
        if (StringUtils.hasText(entityName)) {
            Optional<MappedEntity> entity = entitiesMap.getEntities().stream()
                    .filter(mapped -> mapped.getName().equals(entityName))
                    .findFirst();

            if (entity.isEmpty()) {
                return "No entity named '%s' found in application '%s:%s'.".formatted(entityName, groupId, artifactId);
            }

            return objectMapper.writeValueAsString(entity.get());
        }

        // The whole application is requested: return the profile of every mapped entity.
        return objectMapper.writeValueAsString(entitiesMap.getEntities());
    }
}
