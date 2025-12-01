package com.nucleonforge.axile.master.service.convert.response;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ConditionsFeed;
import com.nucleonforge.axile.master.api.response.ConditionsFeedResponse;

import static com.nucleonforge.axile.master.api.response.ConditionsFeedResponse.ConditionMatch;
import static com.nucleonforge.axile.master.api.response.ConditionsFeedResponse.NegativeCondition;
import static com.nucleonforge.axile.master.api.response.ConditionsFeedResponse.PositiveCondition;

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
