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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.Data;
import lombok.ToString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.persistence.entities.classreading.crosspackage.CrossPackageAssociationParent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EntityMethodsInspector}.
 *
 * @author Dmitry Mazurov
 */
class EntityMethodsInspectorTest {

    @Nested
    class FieldAccess {

        @Test // GH-1633
        void shouldReportAssociationReadDirectlyByToString() {
            assertThat(inspectFieldAssociation(DirectToString.class, "items")).containsExactly("items");
        }

        @Test // GH-1633
        void shouldReportAssociationReadViaGetter() {
            assertThat(inspectFieldAssociation(GetterToString.class, "items")).containsExactly("items");
        }

        @Test // GH-1633
        void shouldNotReportAssociationNotUsedByToString() {
            assertThat(inspectFieldAssociation(SafeEntity.class, "items")).isEmpty();
        }

        @Test // GH-1633
        void shouldReportAssociationReadByPrivateHelper() {
            assertThat(inspectFieldAssociation(PrivateHelperToString.class, "items"))
                    .containsExactly("items");
        }

        @Test // GH-1633
        void shouldReportAssociationReadBySuperclassToString() {
            assertThat(inspectFieldAssociation(ChildWithSuperToString.class, "items"))
                    .containsExactly("items");
        }

        @Test // GH-1633
        void shouldReportInheritedAssociationReadByOverriddenHelper() {
            assertThat(inspectFieldAssociation(ChildOverridingHelper.class, "items"))
                    .containsExactly("items");
        }

        @Test // GH-1633
        void shouldResolveStaticHelperFromDeclaringClass() {
            assertThat(inspectFieldAssociation(ChildHidingStaticHelper.class, "items"))
                    .containsExactly("items");
        }

        @Test // GH-1633
        void shouldNotReplacePrivateSuperclassHelperWithChildMethod() {
            assertThat(inspectFieldAssociation(ChildWithSamePrivateHelper.class, "items"))
                    .containsExactly("items");
        }

        @Test // GH-1633
        void shouldNotTreatSameSignatureMethodInDifferentPackageAsOverride() {
            assertThat(inspectFieldAssociation(ChildInDifferentPackageWithUnrelatedHelper.class, "customer"))
                    .containsExactly("customer");
        }
    }

    @Nested
    class PropertyAccess {

        @Test // GH-1633
        void shouldReportAssociationReadViaPropertyGetter() {
            assertThat(inspectPropertyAssociation(PropertyGetterToString.class, "customer", "getCustomer"))
                    .containsExactly("customer");
        }

        @Test // GH-1633
        void shouldReportInheritedAssociationReadViaSuperGetter() {
            assertThat(inspectPropertyAssociation(PropertyChildWithSuperGetter.class, "customer", "getCustomer"))
                    .containsExactly("customer");
        }

        @Test // GH-1633
        void shouldReportPropertyAssociationWhenToStringReadsBackingFieldDirectly() {
            assertThat(inspectPropertyAssociation(PropertyDirectBackingFieldToString.class, "customer", "getCustomer"))
                    .containsExactly("customer");
        }

        @Test // GH-1633
        void shouldInferBackingFieldWhenGetterReadsAnotherNonAssociationField() {
            assertThat(inspectPropertyAssociation(PropertyGetterWithAdditionalField.class, "customer", "getCustomer"))
                    .containsExactly("customer");
        }

        @Test // GH-1633
        void shouldNotGuessBackingFieldWhenGetterReadsSeveralAssociationTypedFields() {
            assertThat(inspectPropertyAssociation(PropertyAmbiguousGetter.class, "customer", "getCustomer"))
                    .isEmpty();
        }

        @Test // GH-1633
        void shouldNotConfuseForeignKeyFieldWithBackingField() {
            assertThat(inspectPropertyAssociation(PropertyLookupByIdToString.class, "customer", "getCustomer"))
                    .isEmpty();
        }
    }

    @Nested
    class LombokGeneratedToString {

        @Test // GH-1633
        void shouldReportAssociationReadByLombokToString() {
            assertThat(inspectFieldAssociation(LombokToStringEntity.class, "items"))
                    .containsExactly("items");
        }

        @Test // GH-1633
        void shouldNotReportAssociationExcludedFromLombokToString() {
            assertThat(inspectFieldAssociation(LombokExcludedEntity.class, "items"))
                    .isEmpty();
        }

        @Test // GH-1633
        void shouldNotReportAssociationWithoutExplicitInclude() {
            assertThat(inspectFieldAssociation(LombokOnlyExplicitEntity.class, "items"))
                    .isEmpty();
        }

        @Test // GH-1633
        void shouldReportAssociationReadByLombokData() {
            assertThat(inspectFieldAssociation(LombokDataEntity.class, "items")).containsExactly("items");
        }

        @Test // GH-1633
        void shouldReportInheritedAssociationViaCallSuper() {
            assertThat(inspectFieldAssociation(LombokChildEntity.class, "items"))
                    .containsExactly("items");
        }
    }

    private static Set<String> inspectFieldAssociation(Class<?> entityClass, String fieldName) {
        Field field = findField(entityClass, fieldName);

        assertThat(field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class))
                .as("%s must be a real JPA association", field)
                .isTrue();

        return inspect(entityClass, new AssociationMember(fieldName, field));
    }

    private static Set<String> inspectPropertyAssociation(
            Class<?> entityClass, String associationName, String getterName) {
        Method method = findMethod(entityClass, getterName);

        assertThat(method.isAnnotationPresent(ManyToOne.class) || method.isAnnotationPresent(OneToMany.class))
                .as("%s must be a real JPA association", method)
                .isTrue();

        return inspect(entityClass, new AssociationMember(associationName, method));
    }

    private static Set<String> inspect(Class<?> entityClass, AssociationMember association) {
        return new EntityMethodsInspector(entityClass, Set.of(association)).detectAssociationsReadByToString();
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // Continue with superclass.
            }
        }

        throw new IllegalStateException("Field '%s' was not found in %s hierarchy".formatted(name, type.getName()));
    }

    private static Method findMethod(Class<?> type, String name) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name)) {
                    return method;
                }
            }
        }

        throw new IllegalStateException("Method '%s' was not found in %s hierarchy".formatted(name, type.getName()));
    }

    // Associated entities

    static class ItemEntity {

        @Id
        private Long id;
    }

    static class CustomerEntity {

        @Id
        private Long id;
    }

    // Field access

    static class DirectToString {

        @Id
        private Long id;

        @OneToMany
        private List<ItemEntity> items;

        @Override
        public String toString() {
            return "DirectToString{id=" + id + ", items=" + items + "}";
        }
    }

    static class GetterToString {

        @Id
        private Long id;

        @OneToMany
        private List<ItemEntity> items;

        public List<ItemEntity> getItems() {
            return items;
        }

        @Override
        public String toString() {
            return "GetterToString{items=" + getItems() + "}";
        }
    }

    static class SafeEntity {

        @Id
        private Long id;

        @OneToMany
        private List<ItemEntity> items;

        @Override
        public String toString() {
            return "SafeEntity{id=" + id + "}";
        }
    }

    static class PrivateHelperToString {

        @Id
        private Long id;

        @OneToMany
        private List<ItemEntity> items;

        @Override
        public String toString() {
            return describe();
        }

        private String describe() {
            return "PrivateHelperToString{items=" + items + "}";
        }
    }

    static class ParentWithToString {

        @OneToMany
        protected List<ItemEntity> items;

        @Override
        public String toString() {
            return "ParentWithToString{items=" + items + "}";
        }
    }

    static class ChildWithSuperToString extends ParentWithToString {

        @Id
        private Long id;

        @Override
        public String toString() {
            return "ChildWithSuperToString{id=" + id + "} " + super.toString();
        }
    }

    static class ParentCallingOverridableHelper {

        @OneToMany
        protected List<ItemEntity> items;

        @Override
        public String toString() {
            return describe();
        }

        String describe() {
            return "ParentCallingOverridableHelper{}";
        }
    }

    static class ChildOverridingHelper extends ParentCallingOverridableHelper {

        @Id
        private Long id;

        @Override
        String describe() {
            return "ChildOverridingHelper{items=" + items + "}";
        }
    }

    static class ParentWithStaticHelper {

        @OneToMany
        protected List<ItemEntity> items;

        @Override
        public String toString() {
            return describe(this);
        }

        static String describe(ParentWithStaticHelper self) {
            return "ParentWithStaticHelper{items=" + self.items + "}";
        }
    }

    static class ChildHidingStaticHelper extends ParentWithStaticHelper {

        @Id
        private Long id;

        static String describe(ParentWithStaticHelper self) {
            return "ChildHidingStaticHelper{}";
        }
    }

    static class ParentWithPrivateHelper {

        @OneToMany
        protected List<ItemEntity> items;

        @Override
        public String toString() {
            return describe();
        }

        private String describe() {
            return "ParentWithPrivateHelper{items=" + items + "}";
        }
    }

    static class ChildWithSamePrivateHelper extends ParentWithPrivateHelper {

        @Id
        private Long id;

        @SuppressWarnings("unused")
        private String describe() {
            return "ChildWithSamePrivateHelper{}";
        }
    }

    static class ChildInDifferentPackageWithUnrelatedHelper extends CrossPackageAssociationParent {

        @Id
        private Long id;

        /*
         * Same name and signature as CrossPackageAssociationParent.describe(), but declared in a
         * different package, so per JLS 8.4.8.1 this does not override it - it is an unrelated method.
         */
        @SuppressWarnings("unused")
        String describe() {
            return "ChildInDifferentPackageWithUnrelatedHelper{}";
        }
    }

    // Property access

    @Access(AccessType.PROPERTY)
    static class PropertyGetterToString {

        private Long id;
        private CustomerEntity customerRef;

        @Id
        public Long getId() {
            return id;
        }

        @ManyToOne(fetch = FetchType.LAZY)
        public CustomerEntity getCustomer() {
            return customerRef;
        }

        @Override
        public String toString() {
            return "PropertyGetterToString{customer=" + getCustomer() + "}";
        }
    }

    @Access(AccessType.PROPERTY)
    static class PropertyAssociationSuperclass {

        private CustomerEntity customerRef;

        @ManyToOne(fetch = FetchType.LAZY)
        public CustomerEntity getCustomer() {
            return customerRef;
        }
    }

    @Access(AccessType.PROPERTY)
    static class PropertyChildWithSuperGetter extends PropertyAssociationSuperclass {

        private Long id;

        @Id
        public Long getId() {
            return id;
        }

        @Override
        public String toString() {
            return "PropertyChildWithSuperGetter{customer=" + super.getCustomer() + "}";
        }
    }

    @Access(AccessType.PROPERTY)
    static class PropertyDirectBackingFieldToString {

        private Long id;
        private CustomerEntity customerRef;

        @Id
        public Long getId() {
            return id;
        }

        @ManyToOne(fetch = FetchType.LAZY)
        public CustomerEntity getCustomer() {
            return customerRef;
        }

        @Override
        public String toString() {
            return "PropertyDirectBackingFieldToString{customer=" + customerRef + "}";
        }
    }

    @Access(AccessType.PROPERTY)
    static class PropertyGetterWithAdditionalField {

        private Long id;
        private boolean initialized;
        private CustomerEntity customerRef;

        @Id
        public Long getId() {
            return id;
        }

        @ManyToOne(fetch = FetchType.LAZY)
        public CustomerEntity getCustomer() {
            if (!initialized) {
                return null;
            }

            return customerRef;
        }

        @Override
        public String toString() {
            return "PropertyGetterWithAdditionalField{customer=" + customerRef + "}";
        }
    }

    @Access(AccessType.PROPERTY)
    static class PropertyAmbiguousGetter {

        private Long id;
        private CustomerEntity primaryCustomer;
        private CustomerEntity fallbackCustomer;

        @Id
        public Long getId() {
            return id;
        }

        @ManyToOne(fetch = FetchType.LAZY)
        public CustomerEntity getCustomer() {
            if (primaryCustomer != null) {
                return primaryCustomer;
            }

            return fallbackCustomer;
        }

        @Override
        public String toString() {
            return "PropertyAmbiguousGetter{customer=" + primaryCustomer + "}";
        }
    }

    @Access(AccessType.PROPERTY)
    static class PropertyLookupByIdToString {

        private Long id;
        private Long customerId;

        @Id
        public Long getId() {
            return id;
        }

        @ManyToOne(fetch = FetchType.LAZY)
        public CustomerEntity getCustomer() {
            return findCustomer(customerId);
        }

        private CustomerEntity findCustomer(Long customerId) {
            return null;
        }

        @Override
        public String toString() {
            return "PropertyLookupByIdToString{customerId=" + customerId + "}";
        }
    }

    // Lombok

    @ToString
    static class LombokToStringEntity {

        @Id
        private Long id;

        @OneToMany
        private List<ItemEntity> items;
    }

    @ToString
    static class LombokExcludedEntity {

        @Id
        private Long id;

        @OneToMany
        @ToString.Exclude
        private List<ItemEntity> items;
    }

    @ToString(onlyExplicitlyIncluded = true)
    static class LombokOnlyExplicitEntity {

        @Id
        @ToString.Include
        private Long id;

        @OneToMany
        private List<ItemEntity> items;
    }

    @Data
    static class LombokDataEntity {

        @Id
        private Long id;

        @OneToMany
        private List<ItemEntity> items;
    }

    @ToString
    static class LombokParent {

        @OneToMany
        protected List<ItemEntity> items;
    }

    @ToString(callSuper = true)
    static class LombokChildEntity extends LombokParent {

        @Id
        private Long id;
    }
}
