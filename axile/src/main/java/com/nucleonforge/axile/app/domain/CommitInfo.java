package com.nucleonforge.axile.app.domain;

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
