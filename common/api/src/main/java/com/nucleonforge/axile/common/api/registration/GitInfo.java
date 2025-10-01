package com.nucleonforge.axile.common.api.registration;

/**
 * The common git commit info.
 *
 * @author Mikhail Polivakha
 */
public record GitInfo(
        String commitShaShort,
        String branch,
        // TODO: Migrate to Instant later on. The parsing of a timestamp needs to be taken carefully
        String commitTimestamp,
        CommitAuthor commitAuthor) {

    /**
     * Author of the commit information
     *
     * @author Mikhail Polivakha
     */
    public record CommitAuthor(String name, String email) {}
}
