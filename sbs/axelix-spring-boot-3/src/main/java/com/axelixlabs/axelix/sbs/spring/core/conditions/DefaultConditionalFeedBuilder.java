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
package com.axelixlabs.axelix.sbs.spring.core.conditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint.ConditionsDescriptor;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint.MessageAndConditionDescriptor;
import org.springframework.context.ConfigurableApplicationContext;

import com.axelixlabs.axelix.common.api.ConditionsFeed;
import com.axelixlabs.axelix.common.api.ConditionsFeed.ConditionMatch;
import com.axelixlabs.axelix.common.api.ConditionsFeed.NegativeCondition;
import com.axelixlabs.axelix.common.api.ConditionsFeed.PositiveCondition;

/**
 * Class that is capable to assemble the {@link ConditionsFeed}.
 *
 * @author Nikita Kirilov
 * @author Sergey Cherkasov
 */
public class DefaultConditionalFeedBuilder implements ConditionalFeedBuilder {

    private final ConditionsReportEndpoint delegate;
    private final ConditionalTargetUnwrapper targetUnwrapper;

    public DefaultConditionalFeedBuilder(
            ConfigurableApplicationContext context, ConditionalTargetUnwrapper targetUnwrapper) {
        this.delegate = new ConditionsReportEndpoint(context);
        this.targetUnwrapper = targetUnwrapper;
    }

    @Override
    @NonNull
    public ConditionsFeed buildConditionsFeed() {
        ConditionsDescriptor original = delegate.conditions();

        List<PositiveCondition> positiveConditions = new ArrayList<>();
        List<NegativeCondition> negativeConditions = new ArrayList<>();

        original.getContexts().values().forEach(context -> {
            if (context.getPositiveMatches() != null) {
                for (var entry : context.getPositiveMatches().entrySet()) {

                    UnwrappedTarget unwrappedTarget = targetUnwrapper.unwrap(entry.getKey());

                    positiveConditions.add(new PositiveCondition(
                            unwrappedTarget.getClassName(),
                            unwrappedTarget.getMethodName(),
                            convertMatches(entry.getValue())));
                }
            }

            if (context.getNegativeMatches() != null) {
                for (var entry : context.getNegativeMatches().entrySet()) {

                    UnwrappedTarget unwrappedTarget = targetUnwrapper.unwrap(entry.getKey());

                    negativeConditions.add(new NegativeCondition(
                            unwrappedTarget.getClassName(),
                            unwrappedTarget.getMethodName(),
                            convertMatches(entry.getValue().getNotMatched()),
                            convertMatches(entry.getValue().getMatched())));
                }
            }
        });

        return new ConditionsFeed(positiveConditions, negativeConditions);
    }

    private List<ConditionMatch> convertMatches(List<MessageAndConditionDescriptor> matches) {
        return matches.isEmpty()
                ? Collections.emptyList()
                : matches.stream()
                        .map(match -> new ConditionMatch(match.getCondition(), match.getMessage()))
                        .collect(Collectors.toList());
    }
}
