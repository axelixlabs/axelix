package com.axelixlabs.axelix.sbs.spring.core.master.insights;

/**
 * Well known VM options (either non-standard or advanced).
 *
 * @author Mikhail Polivakha
 */
final class WellKnownVmOptions {

    private WellKnownVmOptions() {}

    /**
     * App CDS archive
     */
    static final String SHARED_ARCHIVE_FILE = "SharedArchiveFile";

    /**
     * Aot Cache
     */
    static final String AOT_CACHE_OPTION = "AOTCache";

    /**
     * Project Lilliput, compact object headers
     */
    static final String USE_COMPACT_OBJECT_HEADERS = "UseCompactObjectHeaders";
}
