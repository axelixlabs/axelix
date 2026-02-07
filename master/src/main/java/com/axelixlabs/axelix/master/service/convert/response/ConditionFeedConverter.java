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
package com.axelixlabs.axelix.master.service.convert.response;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.ConditionsFeed;
import com.axelixlabs.axelix.master.api.external.response.ConditionsFeedResponse;

import static com.axelixlabs.axelix.master.api.external.response.ConditionsFeedResponse.ConditionMatch;
import static com.axelixlabs.axelix.master.api.external.response.ConditionsFeedResponse.NegativeCondition;
import static com.axelixlabs.axelix.master.api.external.response.ConditionsFeedResponse.PositiveCondition;

/**
 * The {@link Converter} from {@link ConditionsFeed} to {@link ConditionsFeedResponse}.
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 */
@Service
public class ConditionFeedConverter implements Converter<ConditionsFeed, ConditionsFeedResponse> {

    @Override
    public @NonNull ConditionsFeedResponse convertInternal(@NonNull ConditionsFeed source) {

        List<PositiveCondition> positiveConditions = source.positiveConditions().stream()
                .map(it -> {
                    UnwrappedTarget result = unwrap(it.target());
                    return new PositiveCondition(result.className(), result.methodName(), convertMatches(it.matches()));
                })
                .toList();

        List<NegativeCondition> negativeConditions = source.negativeConditions().stream()
                .map(it -> {
                    UnwrappedTarget unwrap = unwrap(it.target());
                    return new NegativeCondition(
                            unwrap.className(),
                            unwrap.methodName(),
                            convertMatches(it.notMatched()),
                            convertMatches(it.matched()));
                })
                .toList();

        return new ConditionsFeedResponse(positiveConditions, negativeConditions);
    }

    private List<ConditionMatch> convertMatches(List<ConditionsFeed.ConditionMatch> matches) {
        if (matches == null) {
            return Collections.emptyList();
        }
        return matches.stream()
                .map(m -> new ConditionMatch(m.condition(), m.message()))
                .toList();
    }

    // See SpringBootCondition.getClassOrMethodName method in order to understand why we're doing this.
    private static UnwrappedTarget unwrap(String target) {
        if (target.contains("#")) {
            String[] pair = target.split("#");
            return new UnwrappedTarget(pair[0], pair[1]);
        } else {
            return new UnwrappedTarget(target, null);
        }
    }

    record UnwrappedTarget(String className, @Nullable String methodName) {}
}
