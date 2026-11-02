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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.AssociationProblem;
import com.axelixlabs.axelix.sbs.spring.core.persistence.entities.classreading.AssociationMember;
import com.axelixlabs.axelix.sbs.spring.core.persistence.entities.classreading.EntityMethodsInspector;

/**
 * Inspects one JPA entity and reports the problems detected in its associations.
 *
 * @author Dmitry Mazurov
 * @author Mikhail Polivakha
 */
class EntityInspector {

    private static final Logger log = LoggerFactory.getLogger(EntityInspector.class);

    EntityInspection inspect(EntityType<?> entityType) {
        String entityName = entityType.getName();
        Set<String> associationsReadByToString = detectAssociationsReadByToString(entityType, entityName);
        int associationsCount = 0;
        List<AssociationInspection> flaggedAssociations = new ArrayList<>();

        for (Attribute<?, ?> attribute : entityType.getAttributes()) {
            if (!attribute.isAssociation()) {
                continue;
            }

            associationsCount++;

            Member member = attribute.getJavaMember();
            if (!(member instanceof AnnotatedElement annotated)) {
                continue;
            }

            AssociationInspector inspector = new AssociationInspector(
                    attribute, annotated, associationsReadByToString.contains(attribute.getName()));
            Set<AssociationProblem> problems = inspector.detectProblems();

            if (!problems.isEmpty()) {
                flaggedAssociations.add(
                        new AssociationInspection(attribute.getName(), inspector.renderMapping(), problems));
            }
        }

        return new EntityInspection(associationsCount, flaggedAssociations);
    }

    private static Set<String> detectAssociationsReadByToString(EntityType<?> entityType, String entityName) {
        try {
            Set<AssociationMember> associations = collectAssociationMembers(entityType, entityName);
            return new EntityMethodsInspector(entityType.getJavaType(), associations)
                    .detectAssociationsReadByToString();
        } catch (RuntimeException e) {
            log.warn(
                    "Could not detect associations read by toString() for {}, toString inspection is skipped",
                    entityName,
                    e);
            return Set.of();
        }
    }

    private static Set<AssociationMember> collectAssociationMembers(EntityType<?> entityType, String entityName) {
        Set<AssociationMember> associations = new HashSet<>();

        entityType
            .getAttributes()
            .stream()
            .filter(Attribute::isAssociation)
            .forEach(attribute -> {
                try {
                    associations.add(new AssociationMember(attribute.getName(), attribute.getJavaMember()));
                } catch (RuntimeException e) {
                    log.warn(
                        "Could not resolve association member {}.{}, it is skipped",
                        entityName,
                        attribute.getName(),
                        e);
                }
        });

        return associations;
    }


    record EntityInspection(int associationsCount, List<AssociationInspection> flaggedAssociations) {

        EntityInspection {
            flaggedAssociations = List.copyOf(flaggedAssociations);
        }
    }

    record AssociationInspection(String name, String mapping, Set<AssociationProblem> problems) {

        AssociationInspection {
            problems = Set.copyOf(problems);
        }
    }
}
