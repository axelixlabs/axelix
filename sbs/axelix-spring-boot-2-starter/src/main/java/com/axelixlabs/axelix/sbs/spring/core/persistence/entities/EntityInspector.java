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
import java.util.stream.Collectors;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.AssociationProblem;
import com.axelixlabs.axelix.sbs.spring.core.persistence.entities.classreading.AssociationMember;
import com.axelixlabs.axelix.sbs.spring.core.persistence.entities.classreading.EntityMethodsInspector;

/**
 * Inspects one JPA entity and reports the problems detected in its associations.
 *
 * @author Nikita Kirillov
 */
class EntityInspector {

    private static final Logger log = LoggerFactory.getLogger(EntityInspector.class);

    EntityInspection inspect(EntityType<?> entityType) {
        String entityName = entityType.getName();
        List<? extends Attribute<?, ?>> associations = findEntityAssociations(entityType);
        Set<String> associationsReadByToString =
                detectAssociationsReadByToString(entityType.getJavaType(), associations, entityName);
        List<AssociationInspection> flaggedAssociations = new ArrayList<>();

        for (Attribute<?, ?> attribute : associations) {
            Member member = attribute.getJavaMember();

            if (!(member instanceof AnnotatedElement)) {
                continue;
            }

            AssociationInspector inspector = new AssociationInspector(attribute, (AnnotatedElement) member);
            Set<AssociationProblem> problems = inspector.detectProblems();

            if (associationsReadByToString.contains(attribute.getName())) {
                problems.add(AssociationProblem.ACCESSED_IN_TO_STRING);
            }

            if (!problems.isEmpty()) {
                flaggedAssociations.add(
                        new AssociationInspection(attribute.getName(), inspector.renderMapping(), problems));
            }
        }

        return new EntityInspection(associations.size(), flaggedAssociations);
    }

    private static List<? extends Attribute<?, ?>> findEntityAssociations(EntityType<?> entityType) {
        return entityType.getAttributes().stream()
                .filter(Attribute::isAssociation)
                .collect(Collectors.toList());
    }

    private static Set<String> detectAssociationsReadByToString(
            Class<?> entityClass, List<? extends Attribute<?, ?>> associations, String entityName) {
        try {
            Set<AssociationMember> associationMembers = collectAssociationMembers(associations, entityName);
            return new EntityMethodsInspector(entityClass, associationMembers).detectAssociationsReadByToString();
        } catch (RuntimeException e) {
            log.warn(
                    "Could not detect associations read by toString() for {}, toString inspection is skipped",
                    entityName,
                    e);
            return Set.of();
        }
    }

    private static Set<AssociationMember> collectAssociationMembers(
            List<? extends Attribute<?, ?>> associations, String entityName) {
        Set<AssociationMember> associationMembers = new HashSet<>();

        for (Attribute<?, ?> attribute : associations) {
            try {
                associationMembers.add(new AssociationMember(attribute.getName(), attribute.getJavaMember()));
            } catch (RuntimeException e) {
                log.warn(
                        "Could not resolve association member {}.{}, it is skipped",
                        entityName,
                        attribute.getName(),
                        e);
            }
        }

        return associationMembers;
    }

    static final class EntityInspection {

        private final int associationsCount;
        private final List<AssociationInspection> flaggedAssociations;

        EntityInspection(int associationsCount, List<AssociationInspection> flaggedAssociations) {
            this.associationsCount = associationsCount;
            this.flaggedAssociations = List.copyOf(flaggedAssociations);
        }

        int getAssociationsCount() {
            return associationsCount;
        }

        List<AssociationInspection> getFlaggedAssociations() {
            return flaggedAssociations;
        }
    }

    static final class AssociationInspection {

        private final String name;
        private final String mapping;
        private final Set<AssociationProblem> problems;

        AssociationInspection(String name, String mapping, Set<AssociationProblem> problems) {
            this.name = name;
            this.mapping = mapping;
            this.problems = Set.copyOf(problems);
        }

        String getName() {
            return name;
        }

        String getMapping() {
            return mapping;
        }

        Set<AssociationProblem> getProblems() {
            return problems;
        }
    }
}
