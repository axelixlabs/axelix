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
package com.axelixlabs.axelix.master.service;

import java.util.List;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link BulkInstancesResolver}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
class BulkInstancesResolverTest {

    private static final String FIRST_INSTANCE_ID = "first-instance";
    private static final String SECOND_INSTANCE_ID = "second-instance";

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private BulkInstancesResolver resolver;

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(FIRST_INSTANCE_ID));
        registry.deRegister(InstanceId.of(SECOND_INSTANCE_ID));
    }

    @Test
    void shouldReturnSelectedInstancesFromSameApplication() {
        // given.
        registry.register(TestInstanceFactory.create(FIRST_INSTANCE_ID));
        registry.register(TestInstanceFactory.create(SECOND_INSTANCE_ID));

        // when.
        List<Instance> instances = resolver.resolve(List.of(FIRST_INSTANCE_ID, SECOND_INSTANCE_ID));

        // then.
        assertThat(instances)
                .extracting(instance -> instance.id().instanceId())
                .containsExactlyInAnyOrder(FIRST_INSTANCE_ID, SECOND_INSTANCE_ID);
    }

    @Test
    void shouldIgnoreBlankAndDuplicatedIds() {
        // given.
        registry.register(TestInstanceFactory.create(FIRST_INSTANCE_ID));
        registry.register(TestInstanceFactory.create(SECOND_INSTANCE_ID));

        // when.
        List<Instance> instances = resolver.resolve(
                List.of(FIRST_INSTANCE_ID, "", " ", FIRST_INSTANCE_ID, SECOND_INSTANCE_ID, SECOND_INSTANCE_ID));

        // then.
        assertThat(instances)
                .extracting(instance -> instance.id().instanceId())
                .containsExactlyInAnyOrder(FIRST_INSTANCE_ID, SECOND_INSTANCE_ID);
    }

    @Test
    void shouldReturnEmptyListForEmptySelection() {
        // given.
        List<String> ids = List.of();

        // when.
        List<Instance> instances = resolver.resolve(ids);

        // then.
        assertThat(instances).isEmpty();
    }

    @Test
    void shouldRejectSelectionContainingMissingInstances() {
        // given.
        registry.register(TestInstanceFactory.create(FIRST_INSTANCE_ID));

        // when.
        ThrowableAssert.ThrowingCallable callable =
                () -> resolver.resolve(List.of("missing-first", FIRST_INSTANCE_ID, "missing-last", "missing-first"));

        // then.
        assertThatThrownBy(callable).isInstanceOf(InstanceNotFoundException.class);
    }

    @Test
    void shouldRejectSelectionWhenNoInstancesExist() {
        // given.
        List<String> ids = List.of("missing-first", "missing-last");

        // when.
        ThrowableAssert.ThrowingCallable callable = () -> resolver.resolve(ids);

        // then.
        assertThatThrownBy(callable).isInstanceOf(InstanceNotFoundException.class);
    }

    @ParameterizedTest
    @CsvSource({"other-group, first-app", "group, other-app"})
    void shouldRejectInstancesFromDifferentApplications(String groupId, String artifactId) {
        // given.
        registry.register(TestInstanceFactory.create(FIRST_INSTANCE_ID, "group", "first-app"));
        registry.register(TestInstanceFactory.create(SECOND_INSTANCE_ID, groupId, artifactId));

        // when.
        ThrowableAssert.ThrowingCallable callable =
                () -> resolver.resolve(List.of(FIRST_INSTANCE_ID, SECOND_INSTANCE_ID));

        // then.
        assertThatThrownBy(callable).isInstanceOf(BadRequestException.class);
    }
}
