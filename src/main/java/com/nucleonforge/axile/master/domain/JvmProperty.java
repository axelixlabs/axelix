package com.nucleonforge.axile.master.domain;

/**
 * The property being supplied either via -D (e.g {@code -Dmy.custom.property=someValue})
 * or built-in to this JVM.
 *
 * @author Mikhail Polivakha
 */
public record JvmProperty(String key, String value) {}
