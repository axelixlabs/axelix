package com.nucleonforge.axile.common.api.registration;

/**
 * Short information about the build of the given service. Provided during initial scan.
 *
 * @param buildTimestamp the timestamp when this application's build was created
 * @param serviceVersion the version of the <strong>managed service itself</strong>, i.e. the version
 *                of the end-service artifact (the V inside GAV coordinate). The assumption is that
 *                is never {@code null}, and it frankly should not be.
 *
 * @author Mikhail Polivakha
 */
public record ShortBuildInfo(String buildTimestamp, String serviceVersion) {}
