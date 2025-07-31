package com.nucleonforge.axile.master.domain;

import java.time.Instant;

/**
 * Information related to Git commit
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
public class CommitInfo {

    private String commitShaShort;

    private String commitSha;

    private Instant commitTimestamp;

    private String commitAuthorName;

    private String commitAuthorEmail;
}
