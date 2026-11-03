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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

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
 * @author Mikhail Polivakha
 */
public class EntityMethodsInspector {

    private static final Logger log = LoggerFactory.getLogger(EntityMethodsInspector.class);

    // method key, combined from name and method descriptor, see MethodRef
    private static final String TO_STRING_METHOD = "toString()Ljava/lang/String;";

    private final Class<?> entityClass;
    private final String entityInternalName;
    private final Set<AssociationMember> associations;

    private final List<String> hierarchyInternalNames = new ArrayList<>();
    private final Map<String, Class<?>> hierarchyByInternalName = new HashMap<>();
    private final Map<MethodRef, MethodInfo> methods = new HashMap<>();

    private final AtomicBoolean initialized = new AtomicBoolean();

    public EntityMethodsInspector(Class<?> entityClass, Set<AssociationMember> associations) {
        this.entityClass = Objects.requireNonNull(
                entityClass,
                "entityClass is required to locate the entity bytecode; a null entity type is not expected");
        this.entityInternalName = Type.getInternalName(entityClass);
        this.associations = Set.copyOf(associations);
    }

    public Set<String> detectAssociationsReadByToString() {
        if (associations.isEmpty()) {
            return Set.of();
        }

        initialize();

        MethodInfo effectiveToStringMethod = resolveVirtual(new MethodRef(entityInternalName, TO_STRING_METHOD));

        if (effectiveToStringMethod == null) {
            return Set.of();
        }

        return detectAssociations(effectiveToStringMethod);
    }

    private void initialize() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        collectHierarchy();

        for (String internalName : hierarchyInternalNames) {
            readClass(internalName);
        }
    }

    private void collectHierarchy() {
        for (Class<?> type = entityClass; type != null && type != Object.class; type = type.getSuperclass()) {
            String internalName = Type.getInternalName(type);
            hierarchyInternalNames.add(internalName);
            hierarchyByInternalName.put(internalName, type);
        }
    }

    // Traverse the fields/acessors via BFS algorithm
    private Set<String> detectAssociations(MethodInfo toString) {
        Set<String> result = new HashSet<>();

        // visited wavefront
        Set<MethodRef> visited = new HashSet<>();
        Deque<MethodInfo> queue = new ArrayDeque<>();
        queue.add(toString);

        while (!queue.isEmpty()) {
            MethodInfo method = queue.removeFirst();
            // is already visited?
            if (!visited.add(method.ref())) {
                continue;
            }

            detectFieldAssociations(method.body().getReadFields(), result);
            followCalls(method.body().getExactCalls(), this::resolveMethod, result, queue);
            followCalls(method.body().getVirtualCalls(), this::resolveVirtual, result, queue);
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
     * Only fields whose type is assignable to the accessor's return type are considered; the
     * backing field is used only when exactly one such candidate remains.
     */
    private @Nullable FieldRef resolveBackingField(Method accessor) {
        MethodInfo accessorMethod = resolveMethod(MethodRef.from(accessor));
        if (accessorMethod == null) {
            return null;
        }

        List<Field> candidates = collectReadFields(accessorMethod).stream()
                .filter(field -> accessor.getReturnType().isAssignableFrom(field.getType()))
                .toList();

        return candidates.size() == 1 ? FieldRef.from(candidates.get(0)) : null;
    }

    /*
     * Collects fields read by the given method and by any helper it calls (exact or virtual), not
     * just its own body.
     */
    private Set<Field> collectReadFields(MethodInfo root) {
        Set<Field> result = new HashSet<>();
        Set<MethodRef> visited = new HashSet<>();
        Deque<MethodInfo> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            MethodInfo method = queue.removeFirst();
            if (!visited.add(method.ref())) {
                continue;
            }

            method.body().getReadFields().stream()
                    .map(this::resolveJavaField)
                    .filter(Objects::nonNull)
                    .forEach(result::add);
            queueResolvedCalls(method.body().getExactCalls(), this::resolveMethod, queue);
            queueResolvedCalls(method.body().getVirtualCalls(), this::resolveVirtual, queue);
        }

        return result;
    }

    private void queueResolvedCalls(
            Set<MethodRef> calls, Function<MethodRef, @Nullable MethodInfo> resolver, Deque<MethodInfo> queue) {
        for (MethodRef call : calls) {
            MethodInfo target = resolver.apply(call);
            if (target != null) {
                queue.add(target);
            }
        }
    }

    private void followCalls(
            Set<MethodRef> calls,
            Function<MethodRef, @Nullable MethodInfo> resolver,
            Set<String> result,
            Deque<MethodInfo> queue) {
        for (MethodRef call : calls) {
            MethodInfo target = resolver.apply(call);
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

    private @Nullable FieldRef resolveField(FieldRef ref) {
        Field field = resolveJavaField(ref);
        return field != null ? FieldRef.from(field) : null;
    }

    /*
     * The owner stored in a GETFIELD instruction does not necessarily declare the field - e.g.
     * bytecode may reference Child.items even if items is declared by Parent.
     */
    private @Nullable Field resolveJavaField(FieldRef ref) {
        Class<?> owner = hierarchyByInternalName.get(ref.owner());
        if (owner == null) {
            return null;
        }

        for (Class<?> type = owner; type != null && type != Object.class; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(ref.name());
                if (Type.getDescriptor(field.getType()).equals(ref.descriptor())) {
                    return field;
                }
            } catch (NoSuchFieldException ignored) {
                // Continue with superclass.
            }
        }
        return null;
    }

    /*
     * Mirrors JVM symbolic method resolution (JVMS 5.4.3.3): the search starts at ref.owner() and
     * walks up towards Object, not down from the concrete entity type. That is why we need the
     * hierarchyInternalNames - it preserves the hierarchy.
     */
    private @Nullable MethodInfo resolveMethod(MethodRef ref) {
        int ownerIndex = hierarchyInternalNames.indexOf(ref.owner());
        if (ownerIndex < 0) {
            return null;
        }

        for (int i = ownerIndex; i < hierarchyInternalNames.size(); i++) {
            MethodInfo method = methods.get(new MethodRef(hierarchyInternalNames.get(i), ref.key()));
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
        MethodInfo method = resolveMethod(ref);
        if (method == null) {
            return null;
        }

        if (method.cannotBeOverridden()) {
            return method;
        }

        MethodInfo resolved = method;
        int resolvedIndex = hierarchyInternalNames.indexOf(method.ref().owner());

        for (int i = resolvedIndex - 1; i >= 0; i--) {
            MethodInfo candidate = methods.get(new MethodRef(hierarchyInternalNames.get(i), ref.key()));
            if (candidate != null && overrides(candidate, resolved)) {
                resolved = candidate;
            }
        }
        return resolved;
    }

    private static boolean overrides(MethodInfo candidate, MethodInfo overridden) {
        if (candidate.isPrivate() || candidate.isStatic()) {
            return false;
        }

        /*
         * A package-private method can only participate in overriding while the subclass declaring
         * the candidate method belongs to the same package.
         */
        return !overridden.isPackagePrivate()
                || samePackage(candidate.ref().owner(), overridden.ref().owner());
    }

    private static boolean samePackage(String first, String second) {
        return packageName(first).equals(packageName(second));
    }

    private static String packageName(String internalName) {
        int separator = internalName.lastIndexOf('/');
        return separator < 0 ? "" : internalName.substring(0, separator);
    }

    private void readClass(String internalName) {
        Class<?> type = hierarchyByInternalName.get(internalName);
        if (type == null) {
            return;
        }

        ClassLoader classLoader = type.getClassLoader();

        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        try (InputStream stream = classLoader.getResourceAsStream(internalName + ".class")) {
            if (stream == null) {
                log.warn("Could not find bytecode of {}, association inspection is skipped for it", type.getName());
                return;
            }

            MethodBodyReadingClassVisitor visitor =
                    new MethodBodyReadingClassVisitor(internalName, hierarchyByInternalName.keySet());
            new ClassReader(stream).accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            methods.putAll(visitor.getMethods());
        } catch (IOException | RuntimeException e) {
            log.warn("Could not read bytecode of {}, association inspection is skipped for it", type.getName(), e);
        }
    }
}
