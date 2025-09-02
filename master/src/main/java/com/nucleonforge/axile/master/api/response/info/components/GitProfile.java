package com.nucleonforge.axile.master.api.response.info.components;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * The profile of a given build.
 *
 * @param branch     The name of the Git branch.
 * @param commit     The details of the Git commit, if any.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GitProfile(String branch, @Nullable Commit commit) {

    /**
     * The profile of a given commit.
     *
     * @param id     The ID of the commit.
     * @param time   The timestamp of the commit.
     *
     * @author Sergey Cherkasov
     */
    public record Commit(String id, String time) {}
}
