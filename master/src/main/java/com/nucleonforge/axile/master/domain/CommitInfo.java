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

    public CommitInfo(
            String commitShaShort,
            String commitSha,
            Instant commitTimestamp,
            String commitAuthorName,
            String commitAuthorEmail) {
        this.commitShaShort = commitShaShort;
        this.commitSha = commitSha;
        this.commitTimestamp = commitTimestamp;
        this.commitAuthorName = commitAuthorName;
        this.commitAuthorEmail = commitAuthorEmail;
    }
}
