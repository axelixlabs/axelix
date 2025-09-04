package com.nucleonforge.axile.common.domain;

/**
 * General information about the project's build.
 *
 * @author Mikhail Polivakha
 */
public class BuildInfo {

    /**
     * The information about deployed commit.
     */
    private final CommitInfo commitInfo;

    /**
     * ClassPath of the application.
     */
    private final ClassPath classPath;

    public BuildInfo(CommitInfo commitInfo, ClassPath classPath) {
        this.commitInfo = commitInfo;
        this.classPath = classPath;
    }

    public CommitInfo getCommitInfo() {
        return commitInfo;
    }

    public ClassPath getClassPath() {
        return classPath;
    }
}
