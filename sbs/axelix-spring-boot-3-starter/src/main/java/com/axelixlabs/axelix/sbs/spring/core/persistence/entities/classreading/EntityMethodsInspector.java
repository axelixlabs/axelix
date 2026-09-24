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
package com.axelixlabs.axelix.sbs.spring.core.persistence.entities.classreading;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.asm.ClassReader;
import org.springframework.asm.Type;

/**
 * Inspects entity bytecode to detect associations read by {@code toString()}.
 *
 * <p>The inspector follows method calls within the entity class hierarchy and supports both
 * field-based and property-based JPA associations.
 *
 * <p>For property-based associations, the accessor itself is treated as an association access.
 *
 * <p>Only the entity's class hierarchy is followed; associations read through a lambda body or an
 * interface's default method are not detected.
 *
 * @author Dmitry Mazurov
 */
public class EntityMethodsInspector {

    private static final Logger log = LoggerFactory.getLogger(EntityMethodsInspector.class);

    private static final String TO_STRING_METHOD = "toString()Ljava/lang/String;";

    private final Class<?> entityClass;
    private final String entityInternalName;
    private final Set<AssociationMember> associations;

    private final List<Class<?>> hierarchy = new ArrayList<>();
    private final Map<String, Class<?>> hierarchyByInternalName = new HashMap<>();
    private final Map<MethodRef, MethodInfo> methods = new HashMap<>();

    private boolean initialized;

    public EntityMethodsInspector(Class<?> entityClass, Set<AssociationMember> associations) {
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass");
        this.entityInternalName = Type.getInternalName(entityClass);
        this.associations = Set.copyOf(associations);
    }

    public Set<String> detectAssociationsReadByToString() {
        if (associations.isEmpty()) {
            return Set.of();
        }

        initialize();

        MethodInfo root = resolveVirtual(new MethodRef(entityInternalName, TO_STRING_METHOD));
        if (root == null) {
            return Set.of();
        }

        return detectAssociations(root);
    }

    private void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        collectHierarchy();

        for (Class<?> type : hierarchy) {
            readClass(type);
        }
    }

    private void collectHierarchy() {
        for (Class<?> type = entityClass; type != null && type != Object.class; type = type.getSuperclass()) {
            hierarchy.add(type);
            hierarchyByInternalName.put(Type.getInternalName(type), type);
        }
    }

    private Set<String> detectAssociations(MethodInfo root) {
        Set<String> result = new HashSet<>();
        Set<MethodRef> visited = new HashSet<>();
        Deque<MethodInfo> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            MethodInfo method = queue.removeFirst();
            if (!visited.add(method.ref())) {
                continue;
            }

            detectFieldAssociations(method.body().getReadFields(), result);
            followCalls(method.body().getExactCalls(), false, result, queue);
            followCalls(method.body().getVirtualCalls(), true, result, queue);
        }

        return result;
    }

    private void detectFieldAssociations(Set<FieldRef> readFields, Set<String> result) {
        for (FieldRef readField : readFields) {
            FieldRef resolvedField = resolveField(readField);
            if (resolvedField == null) {
                continue;
            }

            for (AssociationMember association : associations) {
                if (matchesFieldAssociation(resolvedField, association)) {
                    result.add(association.name());
                }
            }
        }
    }

    private boolean matchesFieldAssociation(FieldRef field, AssociationMember association) {
        Member member = association.member();
        if (member instanceof Field associationField) {
            return FieldRef.from(associationField).equals(field);
        }

        if (member instanceof Method accessor) {
            FieldRef backingField = resolveBackingField(accessor);
            return field.equals(backingField);
        }

        return false;
    }

    /*
     * If the accessor reads exactly one field, that field is used. If it reads several, the single
     * one whose descriptor matches the accessor's return type is used; otherwise it's ambiguous.
     */
    private @Nullable FieldRef resolveBackingField(Method accessor) {
        MethodInfo accessorMethod = resolveMethod(MethodRef.from(accessor));
        if (accessorMethod == null) {
            return null;
        }

        List<FieldRef> readFields = accessorMethod.body().getReadFields().stream()
                .map(this::resolveField)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (readFields.size() == 1) {
            return readFields.get(0);
        }

        String returnDescriptor = Type.getDescriptor(accessor.getReturnType());

        List<FieldRef> matchingFields = readFields.stream()
                .filter(field -> field.descriptor().equals(returnDescriptor))
                .toList();

        return matchingFields.size() == 1 ? matchingFields.get(0) : null;
    }

    private void followCalls(Set<MethodRef> calls, boolean virtual, Set<String> result, Deque<MethodInfo> queue) {
        for (MethodRef call : calls) {
            MethodInfo target = virtual ? resolveVirtual(call) : resolveMethod(call);
            if (target == null) {
                continue;
            }

            detectMethodAssociation(target, result);
            queue.add(target);
        }
    }

    private void detectMethodAssociation(MethodInfo target, Set<String> result) {
        for (AssociationMember association : associations) {
            Member member = association.member();
            if (!(member instanceof Method accessor)) {
                continue;
            }

            MethodRef accessorRef = MethodRef.from(accessor);

            /*
             * Exact calls such as super.getCustomer() resolve to the declared accessor itself.
             */
            MethodInfo declaredTarget = resolveMethod(accessorRef);

            if (declaredTarget != null && declaredTarget.ref().equals(target.ref())) {
                result.add(association.name());
                continue;
            }

            /*
             * Normal invokevirtual calls may dispatch to an override of the JPA accessor.
             */
            MethodInfo virtualTarget = resolveVirtual(accessorRef);
            if (virtualTarget != null && virtualTarget.ref().equals(target.ref())) {
                result.add(association.name());
            }
        }
    }

    /*
     * The owner stored in a GETFIELD instruction does not necessarily declare the field - e.g.
     * bytecode may reference Child.items even if items is declared by Parent.
     */
    private @Nullable FieldRef resolveField(FieldRef ref) {
        Class<?> owner = hierarchyByInternalName.get(ref.owner());
        if (owner == null) {
            return null;
        }

        for (Class<?> type = owner; type != null && type != Object.class; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(ref.name());
                if (Type.getDescriptor(field.getType()).equals(ref.descriptor())) {
                    return FieldRef.from(field);
                }
            } catch (NoSuchFieldException ignored) {
                // Continue with superclass.
            }
        }
        return null;
    }

    private @Nullable MethodInfo resolveMethod(MethodRef ref) {
        int ownerIndex = hierarchyIndex(ref.owner());
        if (ownerIndex < 0) {
            return null;
        }

        for (int i = ownerIndex; i < hierarchy.size(); i++) {
            String owner = Type.getInternalName(hierarchy.get(i));

            MethodInfo method = methods.get(new MethodRef(owner, ref.key()));
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    /*
     * The symbolic method is resolved first, then valid overrides are applied from the resolved
     * declaring class towards the concrete entity.
     */
    private @Nullable MethodInfo resolveVirtual(MethodRef ref) {
        MethodInfo resolved = resolveMethod(ref);
        if (resolved == null) {
            return null;
        }

        if (resolved.isPrivate() || resolved.isStatic() || resolved.isFinal()) {
            return resolved;
        }

        MethodInfo selected = resolved;
        int resolvedIndex = hierarchyIndex(resolved.ref().owner());

        for (int i = resolvedIndex - 1; i >= 0; i--) {
            String owner = Type.getInternalName(hierarchy.get(i));

            MethodInfo candidate = methods.get(new MethodRef(owner, ref.key()));
            if (candidate != null && overrides(candidate, selected)) {
                selected = candidate;
            }
        }
        return selected;
    }

    private static boolean overrides(MethodInfo candidate, MethodInfo overridden) {
        if (candidate.isPrivate() || candidate.isStatic()) {
            return false;
        }

        if (overridden.isPrivate() || overridden.isStatic() || overridden.isFinal()) {
            return false;
        }

        /*
         * A package-private method can only participate in overriding while the subclass declaring
         * the candidate method belongs to the same package.
         */
        return !overridden.isPackagePrivate()
                || samePackage(candidate.ref().owner(), overridden.ref().owner());
    }

    private int hierarchyIndex(String internalName) {
        for (int i = 0; i < hierarchy.size(); i++) {
            if (Type.getInternalName(hierarchy.get(i)).equals(internalName)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean samePackage(String first, String second) {
        return packageName(first).equals(packageName(second));
    }

    private static String packageName(String internalName) {
        int separator = internalName.lastIndexOf('/');
        return separator < 0 ? "" : internalName.substring(0, separator);
    }

    private void readClass(Class<?> type) {
        String internalName = Type.getInternalName(type);
        ClassLoader classLoader = type.getClassLoader();

        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        try (InputStream stream = classLoader.getResourceAsStream(internalName + ".class")) {
            if (stream == null) {
                log.warn("Could not find bytecode of {}, association inspection is skipped for it", type.getName());
                return;
            }

            MethodBodyReadingClassVisitor visitor = new MethodBodyReadingClassVisitor(
                    internalName, hierarchy.stream().map(Type::getInternalName).toList());
            new ClassReader(stream).accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            methods.putAll(visitor.getMethods());
        } catch (IOException | RuntimeException e) {
            log.warn("Could not read bytecode of {}, association inspection is skipped for it", type.getName(), e);
        }
    }
}
