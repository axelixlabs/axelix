package com.nucleonforge.axile.master.model.software;

/**
 * The specific Software Distribution, i.e. specific version of the {@link SoftwareComponent}.
 *
 * @param softwareComponent the piece of software that the current distribution represents.
 * @param version the specific version/distribution of the given {@link SoftwareComponent}. Can be any
 *        {@link String} like {@code 1.6.2} for library or {@code Amazon inc.} for JDK
 * @author Mikhail Polivakha
 */
public record SoftwareDistribution(SoftwareComponent softwareComponent, String version) {}
