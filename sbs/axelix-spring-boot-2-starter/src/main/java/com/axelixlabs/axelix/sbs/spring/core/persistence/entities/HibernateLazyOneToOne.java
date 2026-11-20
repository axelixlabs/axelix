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

import javax.persistence.EntityManagerFactory;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.OneToOneType;
import org.hibernate.type.Type;

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
        if (!requestsLazyOneToOne(attribute)) {
            return false;
        }

        EntityPersister owner = factory.getMetamodel().entityPersister(entity.getJavaType());
        if (owner.getBytecodeEnhancementMetadata().getLazyAttributesMetadata().isLazyAttribute(attribute.getName())) {
            return false;
        }
        Type propertyType = owner.getPropertyType(attribute.getName());
        if (!(propertyType instanceof org.hibernate.type.EntityType)) {
            return false;
        }
        org.hibernate.type.EntityType associationType = (org.hibernate.type.EntityType) propertyType;
        // A nullable inverse one-to-one needs a query to distinguish a missing row from a proxy.
        if (associationType instanceof OneToOneType && ((OneToOneType) associationType).isNullable()) {
            return true;
        }
        // With no override (null), Hibernate returns its stored eager flag for the mapping that requested LAZY.
        if (associationType.isEager(null)) {
            return true;
        }

        EntityPersister target = factory.getMetamodel().entityPersister(associationType.getAssociatedEntityName());
        return !target.hasProxy() && !target.getBytecodeEnhancementMetadata().isEnhancedForLazyLoading();
    }

    private static boolean requestsLazyOneToOne(Attribute<?, ?> attribute) {
        if (attribute.getPersistentAttributeType() != PersistentAttributeType.ONE_TO_ONE
                || !(attribute.getJavaMember() instanceof AnnotatedElement)) {
            return false;
        }
        // Only mappings that explicitly request lazy loading are candidates for this warning.
        OneToOne annotation = ((AnnotatedElement) attribute.getJavaMember()).getAnnotation(OneToOne.class);
        return annotation != null && annotation.fetch() == FetchType.LAZY;
    }
}
