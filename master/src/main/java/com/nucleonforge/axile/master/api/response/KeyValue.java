package com.nucleonforge.axile.master.api.response;

import org.jspecify.annotations.Nullable;

/**
 * Represents an abstract key-value pair.
 *
 * @since 01.10.2025
 * @author Nikita Kirillov
 */
public record KeyValue(String key, @Nullable String value) {}
