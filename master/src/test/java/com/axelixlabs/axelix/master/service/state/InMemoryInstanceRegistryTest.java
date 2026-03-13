/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.state;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceAlreadyRegisteredException;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;

import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static com.axelixlabs.axelix.master.utils.TestObjectFactory.withName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Unit tests for {@link InMemoryInstanceRegistry}.
 *
 * @since 31.07.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
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
    void shouldFindInstancesByQuery_CommonCase() {
        // given.
        Stream.of(
                        withName("id-9", "invoice-processing"),
                        withName("id-10", "payments"),
                        withName("id-11", "orders-old"),
                        withName("id-12", "commission-processing"))
                .forEach(registry::register);

        // when.
        var result = registry.findByQuery("processing");

        // then.
        assertThat(result).extracting(Instance::id).containsOnly(InstanceId.of("id-9"), InstanceId.of("id-12"));
    }

    @Test
    void shouldFindInstancesByQuery_NoEntriesFound() {
        // given.
        Stream.of(
                        withName("id-9", "invoice-processing"),
                        withName("id-10", "payments"),
                        withName("id-11", "orders-old"),
                        withName("id-12", "commission-processing"))
                .forEach(registry::register);

        // when.
        var result = registry.findByQuery("data");

        // then.
        assertThat(result).isEmpty();
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
