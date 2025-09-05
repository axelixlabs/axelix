package com.nucleonforge.axile.master.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.instancio.Instancio;
import org.instancio.Select;

import com.nucleonforge.axile.common.domain.ClassPath;
import com.nucleonforge.axile.common.domain.ClassPathEntry;
import com.nucleonforge.axile.common.domain.Instance;
import com.nucleonforge.axile.common.domain.InstanceId;

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
        return createInstance(id, DEFAULT_URL);
    }

    public static Instance createInstanceWithUrl(String id, String url) {
        return createInstance(id, url);
    }

    public static Instance createInstance(ClassPathEntry... classPathEntries) {
        return Instancio.of(Instance.class)
                .set(
                        Select.fields().named("classPathEntries").declaredIn(ClassPath.class),
                        Arrays.stream(classPathEntries).collect(Collectors.toSet()))
                .create();
    }

    private static Instance createInstance(String id, String url) {
        return Instancio.of(Instance.class)
                .set(Select.fields().named("id").declaredIn(Instance.class), new InstanceId(id))
                .set(Select.fields().named("actuatorUrl").declaredIn(Instance.class), url)
                .create();
    }
}
