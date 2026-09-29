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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.metamodel.Attribute;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.AssociationProblem;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.JpaEntities;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.MappedEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Integration tests for {@link AssociationInspector}. A real JPA persistence unit is bootstrapped over
 * the fixture entities, and the inspector is driven with the actual metamodel {@link Attribute}s and
 * mapping members it produces — no mocks.
 *
 * @author Mikhail Polivakha
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
                    .containsExactly("Customer", "Order", "OrderEvent", "OrderItem", "Shipment", "Tag");

            // Order carries exactly the problems detected across its associations.
            MappedEntity order = entity(profile, "Order");
            assertThat(order.getTable()).isEqualTo("orders");
            assertThat(order.getAssociationsCount()).isEqualTo(8);
            assertThat(order.getFlaggedAssociations())
                    .extracting(
                            flagged -> flagged.getAssociation().getField(),
                            flagged -> Set.copyOf(flagged.getProblems()))
                    .containsExactlyInAnyOrder(
                            tuple("customer", Set.of(AssociationProblem.EAGER_FETCHING)),
                            tuple("coCustomer", Set.of(AssociationProblem.EAGER_FETCHING)),
                            tuple(
                                    "items",
                                    Set.of(
                                            AssociationProblem.CASCADE_REMOVE_OR_ALL,
                                            AssociationProblem.EAGER_FETCHING)),
                            tuple("tags", Set.of(AssociationProblem.LIST_BACKED_MANY_TO_MANY)),
                            tuple("shipments", Set.of(AssociationProblem.UNIDIRECTIONAL_ONE_TO_MANY)));

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
}
