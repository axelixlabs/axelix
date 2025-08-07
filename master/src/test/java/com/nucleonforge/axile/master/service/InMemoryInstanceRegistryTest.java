package com.nucleonforge.axile.master.service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.master.domain.BuildInfo;
import com.nucleonforge.axile.master.domain.CommitInfo;
import com.nucleonforge.axile.master.domain.Dependencies;
import com.nucleonforge.axile.master.domain.Dependency;
import com.nucleonforge.axile.master.domain.Instance;
import com.nucleonforge.axile.master.domain.JvmNonStandardOption;
import com.nucleonforge.axile.master.domain.JvmNonStandardOptions;
import com.nucleonforge.axile.master.domain.JvmProperties;
import com.nucleonforge.axile.master.domain.JvmProperty;
import com.nucleonforge.axile.master.domain.LaunchDetails;
import com.nucleonforge.axile.master.domain.LoadedClass;
import com.nucleonforge.axile.master.domain.LoadedClasses;
import com.nucleonforge.axile.master.exception.InstanceAlreadyRegisteredException;
import com.nucleonforge.axile.master.exception.NoSuchInstanceException;

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

    private InMemoryInstanceRegistry registry = new InMemoryInstanceRegistry();

    @Test
    void shouldRegisterAndRetrieveInstance() {
        Instance instance = createInstance("id-1");

        registry.register(instance);

        Optional<Instance> optionalInstance = registry.get("id-1");
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

        Optional<Instance> optionalInstance = registry.get("id-3");
        assertTrue(optionalInstance.isPresent());

        registry.deRegister("id-3");

        Optional<Instance> result = registry.get("id-3");
        assertFalse(result.isPresent());
    }

    @Test
    void shouldThrowWhenDeregisterInstanceDoesNotExist() {
        Instance instance = createInstance("id-4");

        registry.register(instance);

        Optional<Instance> optionalInstance = registry.get("id-4");
        assertTrue(optionalInstance.isPresent());

        registry.deRegister("id-4");
        assertThrows(NoSuchInstanceException.class, () -> registry.deRegister("id-4"));
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
        assertThrows(NoSuchInstanceException.class, () -> registry.deRegister("not-existing"));
    }

    @Test
    void shouldThrowIfInstanceToDeregisterNotFound1() {
        assertFalse(registry.get("not-existing").isPresent());
    }

    private Instance createInstance(String id) {
        CommitInfo commitInfo =
                new CommitInfo("commitShaShort", "commitSha", Instant.now(), "authorName", "example@email.com");

        Dependency dependency = new Dependency("testGroupId", "testArtifactId", "testVersion", null, Set.of());
        Dependencies dependencies = new Dependencies(Set.of(dependency));
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

        return new Instance(id, buildInfo, loadedClasses, launchDetails);
    }
}
