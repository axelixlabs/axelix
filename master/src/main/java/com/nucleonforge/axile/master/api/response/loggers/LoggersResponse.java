package com.nucleonforge.axile.master.api.response.loggers;

import java.util.List;
import java.util.Map;

/**
 * The profile of the logging system.
 *
 * @param levels     The levels supported by the current logging system.
 * @param groups     The logger groups keyed by name.
 * @param loggers    The loggers keyed by name.
 *
 * @author Sergey Cherkasov
 */
public record LoggersResponse(
        List<String> levels, Map<String, GroupProfile> groups, Map<String, LoggerProfile> loggers) {}
