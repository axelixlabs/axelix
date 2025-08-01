package com.nucleonforge.axile.master.domain;

public class BuildInfo {

    /**
     * The information about deployed commit.
     */
    private final CommitInfo commitInfo;

    /**
     * Dependencies that are included in the runtime.
     */
    private final Dependencies dependencies;

    public BuildInfo(CommitInfo commitInfo, Dependencies dependencies) {
        this.commitInfo = commitInfo;
        this.dependencies = dependencies;
    }
}
