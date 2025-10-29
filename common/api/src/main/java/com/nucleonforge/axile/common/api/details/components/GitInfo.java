package com.nucleonforge.axile.common.api.details.components;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO that encapsulates the git information of the given artifact.
 *
 * @param commitShaShort     The ID of the commit.
 * @param branch             The name of the Git branch.
 * @param commitAuthor       The commit author information.
 * @param commitTimestamp    The timestamp of the commit.
 *
 * @author Sergey Cherkasov
 */
public record GitInfo(
        @JsonProperty("commitShaShort") String commitShaShort,
        @JsonProperty("branch") String branch,
        @JsonProperty("commitAuthor") CommitAuthor commitAuthor,
        @JsonProperty("commitTimestamp") String commitTimestamp) {

    /**
     * Author of the commit information
     *
     * @param name         The commit author name.
     * @param email        The commit author email.
     *
     * @author Sergey Cherkasov
     */
    public record CommitAuthor(@JsonProperty("name") String name, @JsonProperty("email") String email) {}
}
