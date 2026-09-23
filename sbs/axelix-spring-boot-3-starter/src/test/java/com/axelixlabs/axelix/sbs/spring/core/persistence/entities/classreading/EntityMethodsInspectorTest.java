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

import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.ToString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EntityMethodsInspector}.
 *
 * @author Dmitry Mazurov
 */
class EntityMethodsInspectorTest {

    @Test // GH-1633
    void shouldReturnNothingWhenEntityHasNoAssociations() {
        assertThat(new EntityMethodsInspector(DirectToString.class, Set.of()).detectAssociationsReadByToString())
                .isEmpty();
    }

    @Nested
    class HandWrittenToString {

        @Test // GH-1633
        void shouldReportToStringReadingAssociationDirectly() {
            assertThat(inspect(DirectToString.class)).containsExactly("items");
        }

        @Test // GH-1633
        void shouldReportToStringReadingAssociationViaGetter() {
            assertThat(inspect(GetterToString.class)).containsExactly("items");
        }

        @Test // GH-1633
        void shouldNotReportMethodsThatDoNotTouchAssociations() {
            assertThat(inspect(SafeEntity.class)).isEmpty();
        }

        @Test // GH-1633
        void shouldFollowPrivateHelperInsideEntity() {
            assertThat(inspect(HelperToString.class)).containsExactly("items");
        }

        @Test // GH-1633
        void shouldFollowSuperToString() {
            assertThat(inspect(ChildWithSuperToString.class)).containsExactly("items");
        }

        @Test // GH-1633
        void shouldFollowAnOverriddenHelperCalledFromAMappedSuperclass() {
            assertThat(inspect(ChildOverridingHelper.class)).containsExactly("items");
        }

        @Test // GH-1633
        void shouldMapAGetterCallToItsAssociationNameEvenWhenTheBackingFieldDiffers() {
            Set<String> result = new EntityMethodsInspector(MismatchedAccessorToString.class, Set.of("customer"))
                    .detectAssociationsReadByToString();

            assertThat(result).containsExactly("customer");
        }

        @Test // GH-1633
        void shouldMapASuperGetterCallToItsAssociationNameEvenWhenTheBackingFieldDiffers() {
            Set<String> result = new EntityMethodsInspector(MismatchedAccessorSuperCall.class, Set.of("customer"))
                    .detectAssociationsReadByToString();

            assertThat(result).containsExactly("customer");
        }

        @Test // GH-1633
        void shouldResolveAHiddenStaticHelperExactlyAsCalledNotFromTheConcreteType() {
            assertThat(inspect(ChildHidingStaticHelper.class)).containsExactly("items");
        }
    }

    @Nested
    class LombokGeneratedToString {

        @Test // GH-1633
        void shouldReportAssociationRead() {
            assertThat(inspect(LombokToString.class)).containsExactly("items");
        }

        @Test // GH-1633
        void shouldNotReportExcludedAssociation() {
            assertThat(inspect(LombokExcludedToString.class)).isEmpty();
        }

        @Test // GH-1633
        void shouldNotReportAssociationWithoutExplicitInclude() {
            assertThat(inspect(LombokOnlyExplicitToString.class)).isEmpty();
        }

        @Test // GH-1633
        void shouldReportAssociationReadByData() {
            assertThat(inspect(LombokDataEntity.class)).containsExactly("items");
        }

        @Test // GH-1633
        void shouldReportAssociationInheritedViaCallSuper() {
            assertThat(inspect(LombokChildWithCallSuper.class)).containsExactly("items");
        }
    }

    private static Set<String> inspect(Class<?> entityClass) {
        return new EntityMethodsInspector(entityClass, Set.of("items")).detectAssociationsReadByToString();
    }

    static class DirectToString {

        private Long id;
        private List<String> items;

        @Override
        public String toString() {
            return "DirectToString{id=" + id + ", items=" + items + "}";
        }
    }

    static class GetterToString {

        private Long id;
        private List<String> items;

        public List<String> getItems() {
            return items;
        }

        @Override
        public String toString() {
            return "GetterToString{items=" + getItems() + "}";
        }
    }

    static class SafeEntity {

        private Long id;
        private List<String> items;

        @Override
        public String toString() {
            return "SafeEntity{id=" + id + "}";
        }
    }

    static class HelperToString {

        private Long id;
        private List<String> items;

        @Override
        public String toString() {
            return describe();
        }

        private String describe() {
            return "HelperToString{items=" + items + "}";
        }
    }

    static class ParentWithItems {

        protected List<String> items;

        @Override
        public String toString() {
            return "ParentWithItems{items=" + items + "}";
        }
    }

    static class ChildWithSuperToString extends ParentWithItems {

        private Long id;

        @Override
        public String toString() {
            return "Child{id=" + id + "} " + super.toString();
        }
    }

    static class ParentCallingOverridableHelper {

        protected List<String> items;

        @Override
        public String toString() {
            return describe();
        }

        String describe() {
            return "ParentCallingOverridableHelper{}";
        }
    }

    static class ChildOverridingHelper extends ParentCallingOverridableHelper {

        @Override
        String describe() {
            return "ChildOverridingHelper{items=" + items + "}";
        }
    }

    static class ParentWithHiddenStaticHelper {

        protected List<String> items;

        @Override
        public String toString() {
            return staticHelper(this);
        }

        static String staticHelper(ParentWithHiddenStaticHelper self) {
            return "ParentWithHiddenStaticHelper{items=" + self.items + "}";
        }
    }

    static class ChildHidingStaticHelper extends ParentWithHiddenStaticHelper {

        static String staticHelper(ParentWithHiddenStaticHelper self) {
            return "ChildHidingStaticHelper - unrelated";
        }
    }

    static class MismatchedAccessorToString {

        private Long customerRef;

        public Long getCustomer() {
            return customerRef;
        }

        @Override
        public String toString() {
            return "MismatchedAccessorToString{customer=" + getCustomer() + "}";
        }
    }

    static class MismatchedAccessorSuperclass {

        protected Long customerRef;

        public Long getCustomer() {
            return customerRef;
        }
    }

    static class MismatchedAccessorSuperCall extends MismatchedAccessorSuperclass {

        @Override
        public String toString() {
            return "MismatchedAccessorSuperCall{customer=" + super.getCustomer() + "}";
        }
    }

    @ToString
    static class LombokToString {

        private Long id;
        private List<String> items;
    }

    @ToString
    static class LombokExcludedToString {

        private Long id;

        @ToString.Exclude
        private List<String> items;
    }

    @ToString(onlyExplicitlyIncluded = true)
    static class LombokOnlyExplicitToString {

        @ToString.Include
        private Long id;

        private List<String> items;
    }

    @Data
    static class LombokDataEntity {

        private Long id;
        private List<String> items;
    }

    @ToString
    static class LombokParentWithItems {

        protected List<String> items;
    }

    @ToString(callSuper = true)
    static class LombokChildWithCallSuper extends LombokParentWithItems {

        private Long id;
    }
}
