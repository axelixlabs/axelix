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
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Attribute;

import lombok.ToString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.AssociationProblem;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.FlaggedAssociation;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.JpaEntities;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.MappedEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Integration tests for {@link AssociationInspector}. A real JPA persistence unit is bootstrapped over
 * the fixture entities, and the inspector is driven with the actual metamodel {@link Attribute}s and
 * mapping members it produces — no mocks.
 *
 * @author Mikhail Polivakha
 * @author Dmitry Mazurov
 */
class AssociationInspectorTest {

    private final ApplicationContextRunner jpaContextRunner = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class))
            .withUserConfiguration(JpaTestConfiguration.class);

    @Nested
    class RenderMapping {

        @Test
        void shouldRenderFetchTypeForEagerToOne() {
            // given / when / then.
            jpaContextRunner.run(context -> assertThat(
                            inspect(context, Order.class, "customer").renderMapping())
                    .isEqualTo("@ManyToOne(fetch = FetchType.EAGER)"));
        }

        @Test
        void shouldRenderMappedByAndCascadeAndCollectionElement() {
            // given / when / then.
            jpaContextRunner.run(
                    context -> assertThat(inspect(context, Order.class, "items").renderMapping())
                            .isEqualTo(
                                    "@OneToMany(mappedBy = \"order\", fetch = FetchType.EAGER, cascade = CascadeType.ALL) List<OrderItem>"));
        }

        @Test
        void shouldRenderListBackedManyToMany() {
            // given / when / then.
            jpaContextRunner.run(
                    context -> assertThat(inspect(context, Order.class, "tags").renderMapping())
                            .isEqualTo("@ManyToMany List<Tag>"));
        }

        @Test
        void shouldRenderJoinColumnForUnidirectionalOneToMany() {
            // given / when / then.
            jpaContextRunner.run(context -> assertThat(
                            inspect(context, Order.class, "shipments").renderMapping())
                    .isEqualTo("@OneToMany @JoinColumn(name = \"owner_id\") List<Shipment>"));
        }
    }

    @Test
    void shouldScanEveryEntityWithItsExpectedProblems() {
        jpaContextRunner.run(context -> {
            // given.
            EntityManagerFactory entityManagerFactory = context.getBean(EntityManagerFactory.class);

            // when.
            JpaEntities profile = new EntityMappingScanner(entityManagerFactory).scan();

            // then. every mapped entity is detected, ordered by name.
            assertThat(profile.getEntities())
                    .extracting(MappedEntity::getName)
                    .map(s -> s.substring("AssociationInspectorTest$".length())) // removing prefix
                    .containsExactly("Customer", "LombokOrder", "Order", "OrderEvent", "OrderItem", "Shipment", "Tag");

            // Order carries exactly the problems detected across its associations.
            MappedEntity order = entity(profile, "Order");
            assertThat(order.getTable()).isEqualTo("orders");
            assertThat(order.getAssociationsCount()).isEqualTo(8);
            assertThat(order.getFlaggedAssociations())
                    .extracting(flagged -> flagged.getAssociation().getField(), FlaggedAssociation::getProblems)
                    .containsExactlyInAnyOrder(
                            tuple("customer", Set.of(AssociationProblem.EAGER_FETCHING, AssociationProblem.TO_STRING)),
                            tuple("coCustomer", Set.of(AssociationProblem.EAGER_FETCHING)),
                            tuple(
                                    "items",
                                    Set.of(
                                            AssociationProblem.CASCADE_REMOVE_OR_ALL,
                                            AssociationProblem.EAGER_FETCHING)),
                            tuple("tags", Set.of(AssociationProblem.LIST_BACKED_MANY_TO_MANY)),
                            tuple("shipments", Set.of(AssociationProblem.UNIDIRECTIONAL_ONE_TO_MANY)),
                            tuple("tagSet", Set.of(AssociationProblem.TO_STRING)));

            // the association read by a Lombok-generated toString() is flagged too, through the same path.
            assertThat(entity(profile, "LombokOrder").getFlaggedAssociations())
                    .extracting(flagged -> flagged.getAssociation().getField(), FlaggedAssociation::getProblems)
                    .containsExactly(tuple("customer", Set.of(AssociationProblem.TO_STRING)));

            // every other entity is clean.
            assertThat(entity(profile, "Customer").getFlaggedAssociations()).isEmpty();
            assertThat(entity(profile, "OrderItem").getFlaggedAssociations()).isEmpty();
            assertThat(entity(profile, "OrderEvent").getFlaggedAssociations()).isEmpty();
            assertThat(entity(profile, "Shipment").getFlaggedAssociations()).isEmpty();
            assertThat(entity(profile, "Tag").getFlaggedAssociations()).isEmpty();
        });
    }

    private static MappedEntity entity(JpaEntities profile, String name) {
        return profile.getEntities().stream()
                .filter(mappedEntity -> mappedEntity.getName().equals("AssociationInspectorTest$" + name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No mapped entity named " + name));
    }

    private static AssociationInspector inspect(ApplicationContext context, Class<?> entityType, String attribute) {
        EntityManagerFactory entityManagerFactory = context.getBean(EntityManagerFactory.class);
        Attribute<?, ?> jpaAttribute =
                entityManagerFactory.getMetamodel().entity(entityType).getAttribute(attribute);
        return new AssociationInspector(jpaAttribute, (AnnotatedElement) jpaAttribute.getJavaMember());
    }

    @Configuration(proxyBeanMethods = false)
    @EntityScan(basePackageClasses = Order.class)
    static class JpaTestConfiguration {}

    @Entity
    @Table(name = "orders")
    static class Order {

        @Id
        private Long id;

        @ManyToOne
        private Customer customer;

        @OneToOne(fetch = FetchType.EAGER)
        private Customer coCustomer;

        @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        private Customer manager;

        @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
        private List<OrderItem> items;

        @OneToMany(mappedBy = "order")
        private List<OrderEvent> events;

        @OneToMany
        @JoinColumn(name = "owner_id")
        private List<Shipment> shipments;

        @ManyToMany
        @JoinTable(name = "orders_tags_list")
        private List<Tag> tags;

        @ManyToMany
        @JoinTable(name = "orders_tags_set")
        private Set<Tag> tagSet;

        @Override
        public String toString() {
            return "Order{customer=" + customer + ", tagSet=" + tagSet + "}";
        }
    }

    @Entity
    @Table(name = "customers")
    static class Customer {

        @Id
        private Long id;
    }

    @Entity
    @Table(name = "order_items")
    static class OrderItem {

        @Id
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        private Order order;
    }

    @Entity
    @Table(name = "order_events")
    static class OrderEvent {

        @Id
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        private Order order;
    }

    @Entity
    @Table(name = "shipments")
    static class Shipment {

        @Id
        private Long id;
    }

    @Entity
    @Table(name = "tags")
    static class Tag {

        @Id
        private Long id;
    }

    @Entity
    @Table(name = "lombok_orders")
    @ToString
    static class LombokOrder {

        @Id
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        private Customer customer;
    }
}
