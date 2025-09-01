package com.nucleonforge.axile.master.utils;

import java.time.Instant;
import java.util.Set;

import com.nucleonforge.axile.common.domain.BuildInfo;
import com.nucleonforge.axile.common.domain.ClassPath;
import com.nucleonforge.axile.common.domain.CommitInfo;
import com.nucleonforge.axile.common.domain.Instance;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.JarClassPathEntry;
import com.nucleonforge.axile.common.domain.JvmNonStandardOption;
import com.nucleonforge.axile.common.domain.JvmNonStandardOptions;
import com.nucleonforge.axile.common.domain.JvmProperties;
import com.nucleonforge.axile.common.domain.JvmProperty;
import com.nucleonforge.axile.common.domain.LaunchDetails;
import com.nucleonforge.axile.common.domain.LoadedClass;
import com.nucleonforge.axile.common.domain.LoadedClasses;

/**
 * Utility factory for creating test objects used in unit and integration tests.
 *
 * @since 29.08.2025
 * @author Nikita Kirillov
 */
public final class TestObjectFactory {

    private static final String DEFAULT_URL = "http://example.com";

    private TestObjectFactory() {}

    public static Instance createInstance(String id) {
        return buildInstance(id, DEFAULT_URL);
    }

    public static Instance createInstanceWithUrl(String id, String url) {
        return buildInstance(id, url);
    }

    private static Instance buildInstance(String id, String url) {
        CommitInfo commitInfo =
                new CommitInfo("commitShaShort", "commitSha", Instant.now(), "authorName", "example@email.com");

        JarClassPathEntry dependency =
                new JarClassPathEntry("testGroupId", "testArtifactId", "testVersion", null, Set.of());
        ClassPath dependencies = new ClassPath(Set.of(dependency));
        BuildInfo buildInfo = new BuildInfo(commitInfo, dependencies);

        LoadedClass loadedClass =
                new LoadedClass(ClassLoader.getPlatformClassLoader(), "QualifiedClassName", dependency);
        LoadedClasses loadedClasses = new LoadedClasses(Set.of(loadedClass));

        JvmProperty jvmProperty1 = new JvmProperty("firstKeyJvmProp", "firstValueJvmProp");
        JvmProperty jvmProperty2 = new JvmProperty("secondKeyJvmProp", "secondValueJvmProp");
        JvmProperties jvmProperties = new JvmProperties(Set.of(jvmProperty1, jvmProperty2));

        JvmNonStandardOptions jvmNonStandardOptions = new JvmNonStandardOptions(Set.of(
                new JvmNonStandardOption("firstJvmNonStandardOpt"),
                new JvmNonStandardOption("secondJvmNonStandardOpt")));
        LaunchDetails launchDetails = new LaunchDetails(jvmProperties, jvmNonStandardOptions);

        return new Instance(new InstanceId(id), buildInfo, loadedClasses, launchDetails, url);
    }
}
