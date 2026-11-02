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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.PluralAttribute.CollectionType;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.AssociationProblem;

/**
 * Inspects a single JPA association (an {@link Attribute} together with the {@link AnnotatedElement}
 * mapping it) to detect the {@link AssociationProblem}s in it and render a readable mapping snippet.
 *
 * @author Mikhail Polivakha
 */
class AssociationInspector {

    private static final CascadeType[] NO_CASCADES = new CascadeType[0];

    private final Attribute<?, ?> attribute;
    private final AnnotatedElement member;
    private final PersistentAttributeType type;

    AssociationInspector(Attribute<?, ?> attribute, AnnotatedElement member) {
        this.attribute = attribute;
        this.member = member;
        this.type = attribute.getPersistentAttributeType();
    }

    Set<AssociationProblem> detectProblems() {
        Set<AssociationProblem> problems = new HashSet<>();

        if (effectiveFetch() == FetchType.EAGER) {
            problems.add(AssociationProblem.EAGER_FETCHING);
        }
        if (type == PersistentAttributeType.MANY_TO_MANY && isList()) {
            problems.add(AssociationProblem.LIST_BACKED_MANY_TO_MANY);
        }
        if (isToMany() && hasDangerousCascade()) {
            problems.add(AssociationProblem.CASCADE_REMOVE_OR_ALL);
        }
        if (type == PersistentAttributeType.ONE_TO_MANY && mappedBy().isEmpty()) {
            problems.add(AssociationProblem.UNIDIRECTIONAL_ONE_TO_MANY);
        }

        return problems;
    }

    String renderMapping() {
        List<String> attributes = new ArrayList<>();

        String mappedBy = mappedBy();
        if (!mappedBy.isEmpty()) {
            attributes.add("mappedBy = \"" + mappedBy + "\"");
        }
        if (effectiveFetch() == FetchType.EAGER) {
            attributes.add("fetch = FetchType.EAGER");
        }
        String cascade = renderCascade();
        if (!cascade.isEmpty()) {
            attributes.add("cascade = " + cascade);
        }

        StringBuilder mapping = new StringBuilder("@").append(annotationName());
        if (!attributes.isEmpty()) {
            mapping.append("(").append(String.join(", ", attributes)).append(")");
        }
        mapping.append(renderJoinColumn(mappedBy));
        mapping.append(renderCollectionSuffix());

        return mapping.toString();
    }

    private boolean isToMany() {
        return type == PersistentAttributeType.ONE_TO_MANY || type == PersistentAttributeType.MANY_TO_MANY;
    }

    private boolean isList() {
        return attribute instanceof PluralAttribute<?, ?, ?> plural
                && plural.getCollectionType() == CollectionType.LIST;
    }

    private boolean hasDangerousCascade() {
        for (CascadeType cascade : cascades()) {
            if (cascade == CascadeType.ALL || cascade == CascadeType.REMOVE) {
                return true;
            }
        }
        return false;
    }

    private FetchType effectiveFetch() {
        switch (type) {
            case MANY_TO_ONE:
                ManyToOne manyToOne = member.getAnnotation(ManyToOne.class);
                return manyToOne != null ? manyToOne.fetch() : FetchType.EAGER;
            case ONE_TO_ONE:
                OneToOne oneToOne = member.getAnnotation(OneToOne.class);
                return oneToOne != null ? oneToOne.fetch() : FetchType.EAGER;
            case ONE_TO_MANY:
                OneToMany oneToMany = member.getAnnotation(OneToMany.class);
                return oneToMany != null ? oneToMany.fetch() : FetchType.LAZY;
            case MANY_TO_MANY:
                ManyToMany manyToMany = member.getAnnotation(ManyToMany.class);
                return manyToMany != null ? manyToMany.fetch() : FetchType.LAZY;
            default:
                return FetchType.LAZY;
        }
    }

    private CascadeType[] cascades() {
        switch (type) {
            case MANY_TO_ONE:
                ManyToOne manyToOne = member.getAnnotation(ManyToOne.class);
                return manyToOne != null ? manyToOne.cascade() : NO_CASCADES;
            case ONE_TO_ONE:
                OneToOne oneToOne = member.getAnnotation(OneToOne.class);
                return oneToOne != null ? oneToOne.cascade() : NO_CASCADES;
            case ONE_TO_MANY:
                OneToMany oneToMany = member.getAnnotation(OneToMany.class);
                return oneToMany != null ? oneToMany.cascade() : NO_CASCADES;
            case MANY_TO_MANY:
                ManyToMany manyToMany = member.getAnnotation(ManyToMany.class);
                return manyToMany != null ? manyToMany.cascade() : NO_CASCADES;
            default:
                return NO_CASCADES;
        }
    }

    private String mappedBy() {
        switch (type) {
            case ONE_TO_MANY:
                OneToMany oneToMany = member.getAnnotation(OneToMany.class);
                return oneToMany != null ? oneToMany.mappedBy() : "";
            case ONE_TO_ONE:
                OneToOne oneToOne = member.getAnnotation(OneToOne.class);
                return oneToOne != null ? oneToOne.mappedBy() : "";
            case MANY_TO_MANY:
                ManyToMany manyToMany = member.getAnnotation(ManyToMany.class);
                return manyToMany != null ? manyToMany.mappedBy() : "";
            default:
                return "";
        }
    }

    private String renderJoinColumn(String mappedBy) {
        if (type != PersistentAttributeType.ONE_TO_MANY || !mappedBy.isEmpty()) {
            return "";
        }
        JoinColumn joinColumn = member.getAnnotation(JoinColumn.class);
        if (joinColumn == null || joinColumn.name().isEmpty()) {
            return "";
        }
        return " @JoinColumn(name = \"" + joinColumn.name() + "\")";
    }

    private String renderCollectionSuffix() {
        if (!(attribute instanceof PluralAttribute<?, ?, ?> plural)) {
            return "";
        }
        return " "
                + collectionSimpleName(plural.getCollectionType())
                + "<"
                + plural.getElementType().getJavaType().getSimpleName()
                + ">";
    }

    private String renderCascade() {
        CascadeType[] cascades = cascades();
        if (cascades.length == 0) {
            return "";
        }
        if (cascades.length == 1) {
            return "CascadeType." + cascades[0];
        }
        List<String> rendered = new ArrayList<>();
        for (CascadeType cascade : cascades) {
            rendered.add("CascadeType." + cascade);
        }
        return "{" + String.join(", ", rendered) + "}";
    }

    private String annotationName() {
        switch (type) {
            case ONE_TO_ONE:
                return "OneToOne";
            case ONE_TO_MANY:
                return "OneToMany";
            case MANY_TO_ONE:
                return "ManyToOne";
            case MANY_TO_MANY:
                return "ManyToMany";
            default:
                return type.name();
        }
    }

    private static String collectionSimpleName(CollectionType collectionType) {
        switch (collectionType) {
            case LIST:
                return "List";
            case SET:
                return "Set";
            case MAP:
                return "Map";
            default:
                return "Collection";
        }
    }
}
