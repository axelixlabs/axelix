package com.nucleonforge.axile.master.service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

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
import com.nucleonforge.axile.master.exception.InstanceAlreadyRegisteredException;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.service.state.InMemoryInstanceRegistry;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link InMemoryInstanceRegistry}.
 *
 * @since 31.07.2025
 * @author Nikita Kirillov
 */
class InMemoryInstanceRegistryTest {

    private final InMemoryInstanceRegistry registry = new InMemoryInstanceRegistry();

    @Test
    void shouldRegisterAndRetrieveInstance() {
        Instance instance = createInstance("id-1");

        registry.register(instance);

        Optional<Instance> optionalInstance = registry.get(InstanceId.of("id-1"));
        assertTrue(optionalInstance.isPresent());
        assertEquals(optionalInstance.get(), instance);
    }

    @Test
    void shouldThrowWhenRegisteringInstanceWithDuplicate() {
        Instance instance = createInstance("id-2");

        registry.register(instance);

        assertThrows(InstanceAlreadyRegisteredException.class, () -> {
            registry.register(instance);
        });
    }

    @Test
    void shouldDeregisterInstance() {
        Instance instance = createInstance("id-3");

        assertDoesNotThrow(() -> registry.register(instance));

        Optional<Instance> optionalInstance = registry.get(InstanceId.of("id-3"));
        assertTrue(optionalInstance.isPresent());

        registry.deRegister(InstanceId.of("id-3"));

        Optional<Instance> result = registry.get(InstanceId.of("id-3"));
        assertFalse(result.isPresent());
    }

    @Test
    void shouldThrowWhenDeregisterInstanceDoesNotExist() {
        Instance instance = createInstance("id-4");

        registry.register(instance);

        Optional<Instance> optionalInstance = registry.get(InstanceId.of("id-4"));
        assertTrue(optionalInstance.isPresent());

        registry.deRegister(InstanceId.of("id-4"));
        assertThrows(InstanceNotFoundException.class, () -> registry.deRegister(InstanceId.of("id-4")));
    }

    @Test
    void shouldGetAllInstances() {
        Instance instance1 = createInstance("id-5");
        Instance instance2 = createInstance("id-6");

        registry.register(instance1);
        registry.register(instance2);

        Set<Instance> instances = registry.getAll();

        assertTrue(instances.contains(instance1));
        assertTrue(instances.contains(instance2));
    }

    @Test
    void shouldThrowIfInstanceToDeregisterNotFound() {
        assertThrows(InstanceNotFoundException.class, () -> registry.deRegister(InstanceId.of("not-existing")));
    }

    @Test
    void shouldThrowIfInstanceToDeregisterNotFound1() {
        assertFalse(registry.get(InstanceId.of("not-existing")).isPresent());
    }

    private Instance createInstance(String id) {
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

        return new Instance(new InstanceId(id), buildInfo, loadedClasses, launchDetails, "http://example.com");
    }
}
