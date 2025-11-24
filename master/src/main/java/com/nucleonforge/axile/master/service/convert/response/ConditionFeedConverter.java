package com.nucleonforge.axile.master.service.convert.response;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ConditionsFeed;
import com.nucleonforge.axile.master.api.response.ConditionsFeedResponse;

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
        List<ConditionsFeedResponse.PositiveCondition> positiveConditions = source.positiveConditions().stream()
                .map(positiveCondition -> new ConditionsFeedResponse.PositiveCondition(
                        positiveCondition.target(), convertMatches(positiveCondition.matches())))
                .toList();

        List<ConditionsFeedResponse.NegativeCondition> negativeConditions = source.negativeConditions().stream()
                .map(negativeCondition -> new ConditionsFeedResponse.NegativeCondition(
                        negativeCondition.target(),
                        convertMatches(negativeCondition.notMatched()),
                        convertMatches(negativeCondition.matched())))
                .toList();

        return new ConditionsFeedResponse(positiveConditions, negativeConditions);
    }

    private List<ConditionsFeedResponse.ConditionMatch> convertMatches(List<ConditionsFeed.ConditionMatch> matches) {
        if (matches == null) {
            return Collections.emptyList();
        }
        return matches.stream()
                .map(m -> new ConditionsFeedResponse.ConditionMatch(m.condition(), m.message()))
                .toList();
    }
}
