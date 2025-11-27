package com.nucleonforge.axile.common.domain;

/**
 * General information about the project's build.
 *
 * @author Mikhail Polivakha
 */
public class BuildInfo {

    /**
     * ClassPath of the application.
     */
    private final ClassPath classPath;

    public BuildInfo(ClassPath classPath) {
        this.classPath = classPath;
    }

    public ClassPath getClassPath() {
        return classPath;
    }
}
