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
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.Association;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.AssociationProblem;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.FlaggedAssociation;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.JpaEntities;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.MappedEntity;
import com.axelixlabs.axelix.sbs.spring.core.persistence.entities.classreading.AssociationMember;
import com.axelixlabs.axelix.sbs.spring.core.persistence.entities.classreading.EntityMethodsInspector;

/**
 * Scans the JPA metamodel of an {@link EntityManagerFactory} to build the {@link JpaEntities}: every
 * mapped entity together with the problems detected in its association mappings.
 *
 * <p>The scan relies only on the standard {@code jakarta.persistence} metamodel and annotations, so
 * it works with any JPA provider and does not require Hibernate or Spring Data JPA.
 *
 * @author Mikhail Polivakha
 * @author Dmitry Mazurov
 */
public class EntityMappingScanner {

    private static final Logger log = LoggerFactory.getLogger(EntityMappingScanner.class);

    private final EntityManagerFactory entityManagerFactory;

    public EntityMappingScanner(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * @return the map of every mapped entity, ordered by entity name, with the association problems
     *     detected inside each.
     */
    public JpaEntities scan() {
        List<MappedEntity> entities = new ArrayList<>();

        for (EntityType<?> entityType : entityManagerFactory.getMetamodel().getEntities()) {
            entities.add(scanEntity(entityType));
        }

        entities.sort(Comparator.comparing(MappedEntity::getName));
        return new JpaEntities(entities);
    }

    private MappedEntity scanEntity(EntityType<?> entityType) {
        String entityName = entityType.getName();
        int associationsCount = 0;
        List<FlaggedAssociation> flagged = new ArrayList<>();
        Set<String> associationsReadByToString = new HashSet<>();

        try {
            EntityMethodsInspector methodsInspector = createMethodsInspector(entityType, entityName);
            associationsReadByToString.addAll(methodsInspector.detectAssociationsReadByToString());
        } catch (RuntimeException e) {
            log.warn("Could not detect associations of {}, association inspection is skipped for it", entityName, e);
        }

        for (Attribute<?, ?> attribute : entityType.getAttributes()) {
            if (!attribute.isAssociation()) {
                continue;
            }

            associationsCount++;

            Member member = attribute.getJavaMember();
            if (!(member instanceof AnnotatedElement annotated)) {
                continue;
            }

            AssociationInspector inspector = new AssociationInspector(attribute, annotated);
            Set<AssociationProblem> problems = inspector.detectProblems();
            if (associationsReadByToString.contains(attribute.getName())) {
                problems.add(AssociationProblem.TO_STRING);
            }

            if (!problems.isEmpty()) {
                Association association = new Association(entityName, attribute.getName());
                String mapping = inspector.renderMapping();
                flagged.add(new FlaggedAssociation(association, mapping, problems));
            }
        }

        return new MappedEntity(
                entityName, resolveTable(entityType.getJavaType(), entityName), associationsCount, flagged);
    }

    private static EntityMethodsInspector createMethodsInspector(EntityType<?> entityType, String entityName) {
        Set<AssociationMember> associations = new HashSet<>();

        for (Attribute<?, ?> attribute : entityType.getAttributes()) {
            if (!attribute.isAssociation()) {
                continue;
            }

            try {
                associations.add(new AssociationMember(attribute.getName(), attribute.getJavaMember()));
            } catch (RuntimeException e) {
                log.warn(
                        "Could not resolve association member {}.{}, it is skipped",
                        entityName,
                        attribute.getName(),
                        e);
            }
        }

        return new EntityMethodsInspector(entityType.getJavaType(), associations);
    }

    private static String resolveTable(Class<?> javaType, String entityName) {
        Table table = javaType.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        return entityName;
    }
}
