package com.nucleonforge.axile.master.utils;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.instancio.Instancio;
import org.instancio.Select;

import com.nucleonforge.axile.common.domain.BuildInfo;
import com.nucleonforge.axile.common.domain.ClassPath;
import com.nucleonforge.axile.common.domain.ClassPathEntry;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;

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
        return createInstanceWithUrl(id, DEFAULT_URL);
    }

    // TODO:
    public static Instance createInstanceWithUrl(String id, String url) {
        return new Instance(
                InstanceId.of(id),
                "test-object-factory-instance",
                "1.2.3-classifer-test",
                "17.0.14",
                "3.5.0",
                "df027cf",
                Instant.now(),
                Instance.InstanceStatus.UP,
                url);
    }

    public static BuildInfo createBuildInfo(ClassPathEntry... classPathEntries) {
        return Instancio.of(BuildInfo.class)
                .set(
                        Select.fields().named("classPathEntries").declaredIn(ClassPath.class),
                        Arrays.stream(classPathEntries).collect(Collectors.toSet()))
                .create();
    }
}
