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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpTool.McpAnnotations;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.CountedLazyLoadingTarget;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionAggregatedProfile;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionalKey;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

/**
 * MCP Tools that expose the transactions profile (aggregated information about transactional methods and the
 * persistence problems detected inside them) of a given application.
 *
 * @since 19.07.2026
 * @author Mikhail Polivakha
 */
@Service
public class TransactionsMcpServerTools {

    private final ObjectMapper objectMapper;
    private final DatabaseHistoricalApplicationSnapshotService applicationSnapshotService;

    public TransactionsMcpServerTools(
            ObjectMapper objectMapper, DatabaseHistoricalApplicationSnapshotService applicationSnapshotService) {
        this.objectMapper = objectMapper;
        this.applicationSnapshotService = applicationSnapshotService;
    }

    @McpTool(
            name = McpEndpoints.TRANSACTIONS_PROFILE_TOOL_NAME,
            title = "Transactions Profile",
            description = """
            Get the persistence problems detected inside the transactions of an application: the N+1 lazy loading
            and the in-memory pagination occasions found inside the transactional methods (@Transactional,
            TransactionTemplate, etc.) executed by the application.

            The application is identified by its 'groupId' and 'artifactId' (the G and A of the GAV coordinate of
            the service artifact). Both are REQUIRED. You can typically find them in the build file of the project,
            e.g. in pom.xml or build.gradle or build.gradle.kts.

            You may optionally narrow the result down:
              - provide only the fully qualified class name ('className') to get the problems of every
                transactional method declared in that class;
              - provide both 'className' and 'methodName' to get the problems of that single transactional method.
            'methodName' cannot be provided on its own - it always requires 'className'. When neither is provided,
            the problems of every problematic transaction of the whole application are returned.
        """,
            annotations =
                    @McpAnnotations(
                            title = "Persistence problems detected inside the transactions of an application",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getApplicationTransactionsProfile(
            @McpToolParam(description = "The groupId of the application (the G of the GAV coordinate)") String groupId,
            @McpToolParam(description = "The artifactId of the application (the A of the GAV coordinate)")
                    String artifactId,
            @McpToolParam(required = false, description = """
                    The fully qualified name of the class declaring the transactional method(s) to inspect. When
                    provided without 'methodName', the problems of every transactional method of this class are
                    returned.
                    """) @Nullable String className,
            @McpToolParam(required = false, description = """
                    The name of the transactional method to inspect. Can only be provided together with 'className'.
                    When both are provided, only the problems of this single transactional method are returned.
                    """) @Nullable String methodName) {

        HistoricalApplicationSnapshot snapshot =
                applicationSnapshotService.getCurrentRecord(ApplicationId.of(groupId, artifactId));

        if (snapshot == null) {
            return "No application found with groupId '%s' and artifactId '%s'.".formatted(groupId, artifactId);
        }

        PersistenceInsights insights = snapshot.insights().persistenceInsights();

        if (insights.getTransactions() == null) {
            return "No transactions profile has been recorded for application '%s:%s'.".formatted(groupId, artifactId);
        }

        boolean classNameProvided = StringUtils.hasText(className);
        boolean methodNameProvided = StringUtils.hasText(methodName);

        if (methodNameProvided && !classNameProvided) {
            return "'methodName' can only be provided together with 'className'.";
        }

        // A single transactional method is requested: return the problems of that one transaction.
        if (methodNameProvided) {
            Optional<TransactionAggregatedProfile> transaction = insights.getTransactions().stream()
                    .filter(profile -> matchesMethod(profile.getTransactionalKey(), className, methodName))
                    .findFirst();

            if (transaction.isEmpty()) {
                return "No transaction found for method '%s#%s' in application '%s:%s'."
                        .formatted(className, methodName, groupId, artifactId);
            }

            return objectMapper.writeValueAsString(List.of(toProblems(transaction.get())));
        }

        // Either the whole application or a single class is requested: return the problems of every matching,
        // problematic transaction.
        List<TransactionProblems> problems = insights.getTransactions().stream()
                .filter(profile -> !classNameProvided || matchesClass(profile.getTransactionalKey(), className))
                .map(TransactionsMcpServerTools::toProblems)
                .filter(TransactionProblems::hasProblems)
                .toList();
        return objectMapper.writeValueAsString(problems);
    }

    private static boolean matchesMethod(
            @Nullable TransactionalKey key, @Nullable String className, @Nullable String methodName) {
        return key != null
                && key.getClassName().equals(className)
                && key.getMethodName().equals(methodName);
    }

    private static boolean matchesClass(@Nullable TransactionalKey key, @Nullable String className) {
        return key != null && key.getClassName().equals(className);
    }

    /**
     * Extracts only the persistence problems out of a transaction: the N+1 lazy-loading occasions (an association is
     * an N+1 only when it was lazily loaded more than once, i.e. {@code count > 1}) and the in-memory paginated
     * queries.
     */
    private static TransactionProblems toProblems(TransactionAggregatedProfile transaction) {
        List<CountedLazyLoadingTarget> lazyLoadingTargets =
                transaction.getLazyLoadingTargets() == null ? List.of() : transaction.getLazyLoadingTargets();

        List<NPlusOneProblem> nPlusOne = lazyLoadingTargets.stream()
                .filter(target -> target.getCount() > 1)
                .map(target -> new NPlusOneProblem(
                        target.getTarget().getOwnerEntityClass(),
                        target.getTarget().getAssociationPropertyName()))
                .toList();

        Map<String, Integer> inMemoryPagination =
                transaction.getInMemoryPagination() == null ? Map.of() : transaction.getInMemoryPagination();

        return new TransactionProblems(
                transaction.getTransactionalKey(), nPlusOne, List.copyOf(inMemoryPagination.keySet()));
    }

    /**
     * The persistence problems detected inside a single transaction.
     *
     * @param transactionalKey          the transactional method the problems belong to
     * @param nPlusOne                  the N+1 lazy-loading occasions detected inside the transaction
     * @param inMemoryPaginatedEntities the entities (tables) whose queries were paginated in memory inside the
     *                                  transaction
     */
    public record TransactionProblems(
            TransactionalKey transactionalKey, List<NPlusOneProblem> nPlusOne, List<String> inMemoryPaginatedEntities) {

        boolean hasProblems() {
            return !nPlusOne.isEmpty() || !inMemoryPaginatedEntities.isEmpty();
        }
    }

    /**
     * A single N+1 lazy-loading occasion. Only the fact that the association is loaded as an N+1 is communicated;
     * the exact number of extra queries is deliberately omitted.
     *
     * @param ownerEntityClass        the fully qualified name of the entity that owns the lazily loaded association
     * @param associationPropertyName the name of the lazily loaded association
     */
    public record NPlusOneProblem(String ownerEntityClass, String associationPropertyName) {}
}
