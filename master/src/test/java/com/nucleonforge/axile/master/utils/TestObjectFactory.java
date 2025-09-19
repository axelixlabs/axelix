package com.nucleonforge.axile.master.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.instancio.Instancio;
import org.instancio.Select;

import com.nucleonforge.axile.common.domain.BuildInfo;
import com.nucleonforge.axile.common.domain.ClassPath;
import com.nucleonforge.axile.common.domain.ClassPathEntry;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.InstanceReference;

/**
 * Utility factory for creating test objects used in unit and integration tests.
 *
 * @since 29.08.2025
 * @author Nikita Kirillov
 */
public final class TestObjectFactory {

    private static final String DEFAULT_URL = "http://example.com";

    private TestObjectFactory() {}

    public static InstanceReference createInstance(String id) {
        return createInstanceWithUrl(id, DEFAULT_URL);
    }

    public static InstanceReference createInstanceWithUrl(String id, String url) {
        return new InstanceReference(InstanceId.of(id), url);
    }

    public static BuildInfo createBuildInfo(ClassPathEntry... classPathEntries) {
        return Instancio.of(BuildInfo.class)
                .set(
                        Select.fields().named("classPathEntries").declaredIn(ClassPath.class),
                        Arrays.stream(classPathEntries).collect(Collectors.toSet()))
                .create();
    }
}
