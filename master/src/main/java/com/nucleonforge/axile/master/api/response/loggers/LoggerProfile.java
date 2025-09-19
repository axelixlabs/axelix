package com.nucleonforge.axile.master.api.response.loggers;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * Profile of the active logger.
 *
 * @param configuredLevel  The configured logger level, if any.
 * @param effectiveLevel   The logger's current level.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoggerProfile(@Nullable String configuredLevel, String effectiveLevel) {}
