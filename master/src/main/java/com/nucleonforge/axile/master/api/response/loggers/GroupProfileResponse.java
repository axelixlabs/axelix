package com.nucleonforge.axile.master.api.response.loggers;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * The profile of a group with its loggers.
 *
 * @param configuredLevel   The configured level of the logger group, if any.
 * @param members           The loggers that are part of this group.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GroupProfileResponse(@Nullable String configuredLevel, List<String> members) {}
