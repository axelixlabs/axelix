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
package com.axelixlabs.axelix.sbs.spring.core.persistence.entities;

import java.lang.reflect.Member;
import java.util.Set;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.axelixlabs.axelix.common.contract.metadata.JpaEntities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EntityMappingScannerTest {

    @ParameterizedTest // GH-2031
    @ValueSource(booleans = {true, false})
    void detectsOrdinalEnumsOnPersistentFieldsAndGetters(boolean propertyAccess) throws ReflectiveOperationException {
        // given.
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        Metamodel metamodel = mock(Metamodel.class);
        @SuppressWarnings("unchecked")
        EntityType<SampleEntity> entityType = mock(EntityType.class);
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);
        when(metamodel.getEntities()).thenReturn(Set.of(entityType));
        when(entityType.getJavaType()).thenReturn(SampleEntity.class);
        when(entityType.getName()).thenReturn("SampleEntity");
        Set<Attribute<? super SampleEntity, ?>> attributes = Set.of(
                attribute("implicitOrdinal", propertyAccess),
                attribute("explicitOrdinal", propertyAccess),
                attribute("stringMapping", propertyAccess),
                attribute("unannotated", propertyAccess));
        when(entityType.getAttributes()).thenReturn(attributes);

        // when.
        JpaEntities result = new EntityMappingScanner(entityManagerFactory).scan();

        // then.
        assertThat(result.getEntities()).singleElement().satisfies(entity -> {
            assertThat(entity.getOrdinalEnumAttributes()).containsExactly("explicitOrdinal", "implicitOrdinal");
            assertThat(entity.getAssociationsCount()).isZero();
            assertThat(entity.getFlaggedAssociations()).isEmpty();
        });
    }

    private static Attribute<SampleEntity, Status> attribute(String name, boolean propertyAccess)
            throws ReflectiveOperationException {
        Member member = propertyAccess
                ? SampleEntity.class.getDeclaredMethod(
                        "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1))
                : SampleEntity.class.getDeclaredField(name);
        @SuppressWarnings("unchecked")
        Attribute<SampleEntity, Status> attribute = mock(Attribute.class);
        when(attribute.getName()).thenReturn(name);
        when(attribute.getJavaMember()).thenReturn(member);
        return attribute;
    }

    private enum Status {
        ACTIVE
    }

    private static class SampleEntity {

        @Enumerated
        private Status implicitOrdinal;

        @Enumerated(EnumType.ORDINAL)
        private Status explicitOrdinal;

        @Enumerated(EnumType.STRING)
        private Status stringMapping;

        private Status unannotated;

        @Enumerated
        public Status getImplicitOrdinal() {
            return implicitOrdinal;
        }

        @Enumerated(EnumType.ORDINAL)
        public Status getExplicitOrdinal() {
            return explicitOrdinal;
        }

        @Enumerated(EnumType.STRING)
        public Status getStringMapping() {
            return stringMapping;
        }

        public Status getUnannotated() {
            return unannotated;
        }
    }
}
