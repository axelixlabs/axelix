package com.nucleonforge.axile.master.service;

import java.time.Instant;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
        String id = "id-1";
        Instance instance = createInstance(id);
        registry.register(instance);

        assertThat(registry.get(InstanceId.of(id))).isPresent().contains(instance);
    }

    @Test
    void shouldThrowWhenRegisteringInstanceWithDuplicate() {
        Instance instance = createInstance("id-2");
        registry.register(instance);

        assertThatExceptionOfType(InstanceAlreadyRegisteredException.class)
                .isThrownBy(() -> registry.register(instance));
    }

    @Test
    void shouldDeregisterInstance() {
        String id = "id-3";
        Instance instance = createInstance(id);

        assertThatCode(() -> registry.register(instance)).doesNotThrowAnyException();
        assertThat(registry.get(InstanceId.of(id))).isPresent();

        registry.deRegister(InstanceId.of(id));

        assertThat(registry.get(InstanceId.of(id))).isNotPresent();
    }

    @Test
    void shouldThrowWhenDeregisterInstanceDoesNotExist() {
        String id = "id-4";
        Instance instance = createInstance(id);
        registry.register(instance);

        assertThat(registry.get(InstanceId.of(id))).isPresent();

        registry.deRegister(InstanceId.of(id));

        assertThatExceptionOfType(InstanceNotFoundException.class)
                .isThrownBy(() -> registry.deRegister(InstanceId.of(id)));
    }

    @Test
    void shouldGetAllInstances() {
        Instance instance1 = createInstance("id-5");
        Instance instance2 = createInstance("id-6");

        registry.register(instance1);
        registry.register(instance2);

        assertThat(registry.getAll()).containsOnly(instance1, instance2);
    }

    @Test
    void shouldThrowIfInstanceToDeregisterNotFound() {
        assertThatExceptionOfType(InstanceNotFoundException.class)
                .isThrownBy(() -> registry.deRegister(InstanceId.of("not-existing")));
    }

    @Test
    void shouldThrowIfInstanceToDeregisterNotFound1() {
        assertThat(registry.get(InstanceId.of("not-existing"))).isEmpty();
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
