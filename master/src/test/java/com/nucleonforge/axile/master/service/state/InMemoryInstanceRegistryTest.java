package com.nucleonforge.axile.master.service.state;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.master.exception.InstanceAlreadyRegisteredException;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
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
        String id = "id-2";
        Instance instance = createInstance(id);
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
    void shouldReplaceExistingInstance() {
        String id = "id-7";
        Instance first = createInstance(id);
        Instance second = createInstance(first.id().instanceId());
        registry.register(first);

        // when.
        registry.replace(second);

        // then.
        Instance actual = registry.get(InstanceId.of(id)).orElse(null);
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(second);
    }

    @Test
    void shouldReplaceInstanceWhenItDoesNotExist() {
        String id = "id-8";
        Instance instance = createInstance(id);
        assertThat(registry.get(InstanceId.of(id))).isEmpty();

        // when.
        registry.replace(instance);

        // then
        Instance actual = registry.get(InstanceId.of(id)).orElse(null);
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(instance);
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
}
