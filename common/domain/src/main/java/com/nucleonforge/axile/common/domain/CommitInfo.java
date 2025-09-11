package com.nucleonforge.axile.common.domain;

import java.time.Instant;

/**
 * Information related to Git commit.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
public class CommitInfo {

    private final String commitShaShort;

    private final String commitSha;

    private final Instant commitTimestamp;

    private final String commitAuthorName;

    private final String commitAuthorEmail;

    public String getCommitShaShort() {
        return commitShaShort;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public Instant getCommitTimestamp() {
        return commitTimestamp;
    }

    public String getCommitAuthorName() {
        return commitAuthorName;
    }

    public String getCommitAuthorEmail() {
        return commitAuthorEmail;
    }

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
