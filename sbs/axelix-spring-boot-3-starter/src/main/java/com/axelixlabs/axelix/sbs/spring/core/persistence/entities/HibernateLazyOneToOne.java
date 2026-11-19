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

import java.lang.reflect.AnnotatedElement;
import java.util.function.BiPredicate;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.EntityType;

import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.internal.ToOneAttributeMapping;
import org.hibernate.persister.entity.EntityPersister;

/**
 * Detects one-to-one associations whose lazy fetch request Hibernate cannot honor.
 */
final class HibernateLazyOneToOne {

    private HibernateLazyOneToOne() {}

    static BiPredicate<EntityType<?>, Attribute<?, ?>> check(EntityManagerFactory entityManagerFactory) {
        try {
            SessionFactoryImplementor factory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
            return (entity, attribute) -> isIneffective(factory, entity, attribute);
        } catch (PersistenceException ignored) {
            return (entity, attribute) -> false;
        }
    }

    private static boolean isIneffective(
            SessionFactoryImplementor factory, EntityType<?> entity, Attribute<?, ?> attribute) {
        if (attribute.getPersistentAttributeType() != PersistentAttributeType.ONE_TO_ONE
                || !(attribute.getJavaMember() instanceof AnnotatedElement member)) {
            return false;
        }
        // Only mappings that explicitly request lazy loading are candidates for this warning.
        OneToOne annotation = member.getAnnotation(OneToOne.class);
        if (annotation == null || annotation.fetch() != FetchType.LAZY) {
            return false;
        }

        EntityPersister owner = factory.getMappingMetamodel().getEntityDescriptor(entity.getJavaType());
        if (owner.getBytecodeEnhancementMetadata().getLazyAttributesMetadata().isLazyAttribute(attribute.getName())) {
            return false;
        }
        if (!(owner.findAttributeMapping(attribute.getName()) instanceof ToOneAttributeMapping mapping)) {
            return false;
        }
        if (mapping.getMappedFetchOptions().getTiming() == FetchTiming.IMMEDIATE) {
            return true;
        }

        EntityPersister target = mapping.getEntityMappingType().getEntityPersister();
        return !target.hasProxy() && !target.getBytecodeEnhancementMetadata().isEnhancedForLazyLoading();
    }
}
