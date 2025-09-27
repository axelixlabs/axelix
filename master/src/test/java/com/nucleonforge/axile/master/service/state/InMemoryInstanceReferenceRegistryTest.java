package com.nucleonforge.axile.master.service.state;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.InstanceReference;
import com.nucleonforge.axile.master.exception.InstanceAlreadyRegisteredException;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;

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
class InMemoryInstanceReferenceRegistryTest {

    private final InMemoryInstanceRegistry registry = new InMemoryInstanceRegistry();

    @Test
    void shouldRegisterAndRetrieveInstance() {
        String id = "id-1";
        InstanceReference instanceReference = createInstance(id);
        registry.register(instanceReference);

        assertThat(registry.get(InstanceId.of(id))).isPresent().contains(instanceReference);
    }

    @Test
    void shouldThrowWhenRegisteringInstanceWithDuplicate() {
        String id = "id-2";
        InstanceReference instanceReference = createInstance(id);
        registry.register(instanceReference);

        assertThatExceptionOfType(InstanceAlreadyRegisteredException.class)
                .isThrownBy(() -> registry.register(instanceReference));
    }

    @Test
    void shouldDeregisterInstance() {
        String id = "id-3";
        InstanceReference instanceReference = createInstance(id);

        assertThatCode(() -> registry.register(instanceReference)).doesNotThrowAnyException();
        assertThat(registry.get(InstanceId.of(id))).isPresent();

        registry.deRegister(InstanceId.of(id));

        assertThat(registry.get(InstanceId.of(id))).isNotPresent();
    }

    @Test
    void shouldThrowWhenDeregisterInstanceDoesNotExist() {
        String id = "id-4";
        InstanceReference instanceReference = createInstance(id);
        registry.register(instanceReference);

        assertThat(registry.get(InstanceId.of(id))).isPresent();

        registry.deRegister(InstanceId.of(id));

        assertThatExceptionOfType(InstanceNotFoundException.class)
                .isThrownBy(() -> registry.deRegister(InstanceId.of(id)));
    }

    @Test
    void shouldGetAllInstances() {
        InstanceReference instanceReference1 = createInstance("id-5");
        InstanceReference instanceReference2 = createInstance("id-6");

        registry.register(instanceReference1);
        registry.register(instanceReference2);

        assertThat(registry.getAll()).containsOnly(instanceReference1, instanceReference2);
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
