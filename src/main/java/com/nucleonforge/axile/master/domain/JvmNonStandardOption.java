package com.nucleonforge.axile.master.domain;

/**
 * The option that were supplied via either the {@code -X} or via {@code -XX} to the virtual
 * machine during launch.
 *
 * @param option option as it is. Potentially a key value pair.
 * @author Mikhail Polivakha
 */
public record JvmNonStandardOption(String option) {}
