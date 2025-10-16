package com.nucleonforge.axile.sbs.spring.conditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;

/**
 * Web extension for the {@link ConditionsReportEndpoint}.
 * <p>
 * This class exists to provide a simplified, flattened representation of
 * auto-configuration condition reports for web consumers of the Actuator endpoint.
 * </p>
 *
 * @since 20.10.2025
 * @author Nikita Kirillov
 */
@EndpointWebExtension(endpoint = ConditionsReportEndpoint.class)
public class ConditionsReportEndpointExtension {

    private final ConditionsReportEndpoint delegate;

    public ConditionsReportEndpointExtension(ConditionsReportEndpoint delegate) {
        this.delegate = delegate;
    }

    @ReadOperation
    public FlattenedConditionsDescriptor conditions() {
        ConditionsReportEndpoint.ConditionsDescriptor original = delegate.conditions();

        List<PositiveCondition> positiveConditions = new ArrayList<>();
        List<NegativeCondition> negativeConditions = new ArrayList<>();

        original.getContexts().values().forEach(context -> {
            if (context.getPositiveMatches() != null) {
                for (Map.Entry<String, List<ConditionsReportEndpoint.MessageAndConditionDescriptor>> entry :
                        context.getPositiveMatches().entrySet()) {
                    positiveConditions.add(new PositiveCondition(entry.getKey(), convertMatches(entry.getValue())));
                }
            }

            if (context.getNegativeMatches() != null) {
                for (Map.Entry<String, ConditionsReportEndpoint.MessageAndConditionsDescriptor> entry :
                        context.getNegativeMatches().entrySet()) {
                    negativeConditions.add(new NegativeCondition(
                            entry.getKey(),
                            convertMatches(entry.getValue().getNotMatched()),
                            convertMatches(entry.getValue().getMatched())));
                }
            }
        });

        return new FlattenedConditionsDescriptor(positiveConditions, negativeConditions);
    }

    private List<ConditionMatch> convertMatches(List<ConditionsReportEndpoint.MessageAndConditionDescriptor> matches) {
        if (matches == null) {
            return Collections.emptyList();
        }
        return matches.stream()
                .map(m -> new ConditionMatch(m.getCondition(), m.getMessage()))
                .toList();
    }

    public record FlattenedConditionsDescriptor(
            List<PositiveCondition> positiveConditions, List<NegativeCondition> negativeConditions) {}

    public record PositiveCondition(String target, List<ConditionMatch> matches) {}

    public record NegativeCondition(String target, List<ConditionMatch> notMatched, List<ConditionMatch> matched) {}

    public record ConditionMatch(String condition, String message) {}
}
