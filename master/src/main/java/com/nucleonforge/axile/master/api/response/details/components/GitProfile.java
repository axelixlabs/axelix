package com.nucleonforge.axile.master.api.response.details.components;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The profile of a given build.
 *
 * @param commitShaShort     The ID of the commit.
 * @param branch             The name of the Git branch.
 * @param authorName         The commit author name.
 * @param authorEmail        The commit author email.
 * @param commitTimestamp    The timestamp of the commit.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GitProfile(
        String commitShaShort, String branch, String authorName, String authorEmail, String commitTimestamp) {}
