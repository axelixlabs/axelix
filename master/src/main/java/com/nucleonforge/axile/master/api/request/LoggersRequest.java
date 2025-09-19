package com.nucleonforge.axile.master.api.request;

/**
 * Request to change the logging level of a logger or a logger group.
 *
 * @param configuredLevel   The new logging level to apply.
 *
 * @author Sergey Cherkasov
 */
public record LoggersRequest(String configuredLevel) {}
