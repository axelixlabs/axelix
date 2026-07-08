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
package com.axelixlabs.axelix.sbs.spring.core.persistence;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import io.micrometer.core.instrument.MeterRegistry;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.metrics.DefaultAxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LogbackInMemoryPaginationAppenderRegistrar;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneCollectionLoadListener;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneEntityLoadListener;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneIntegrator;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.DefaultTransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.DefaultTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

import static org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.INTEGRATOR_PROVIDER;

/**
 * Base Spring Boot Test context class for the transaction-monitoring integration tests.
 * It owns the {@link SpringBootTest} declaration and the shared {@link TestConfiguration},
 * so that all subclasses resolve to an identical merged context configuration and therefore share a
 * single cached {@link org.springframework.context.ApplicationContext}.
 *
 * <p>The context runs with a {@link SpringBootTest.WebEnvironment#RANDOM_PORT} web server and exposes the
 * transaction-monitoring actuator endpoint, so the endpoint integration test shares the very same cached
 * context as the non-web bean-post-processor tests. <strong>All</strong> test configurations and fixtures
 * consumed by the transaction-monitoring tests live here, in the parent, as nested classes — so the parent
 * owns the entire shared configuration and never has to reference its subclasses.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.endpoints.web.exposure.include=axelix-transactions-monitoring"})
@Import({
    AbstractTransactionMonitoringSharedContextTest.SharedTransactionTestConfiguration.class,
    JwtAuthTestConfiguration.class
})
abstract class AbstractTransactionMonitoringSharedContextTest {

    @TestConfiguration
    @EnableJpaRepositories(basePackageClasses = OwnerRepository.class, considerNestedRepositories = true)
    @EntityScan(basePackageClasses = {Owner.class, Pet.class})
    static class SharedTransactionTestConfiguration {

        @Bean
        public TransactionMonitoringEndpoint transactionMonitoringEndpoint(
                TransactionMonitoringService transactionMonitoringService) {
            return new TransactionMonitoringEndpoint(transactionMonitoringService);
        }

        @Bean
        public TransactionMonitoringService transactionMonitoringService(
                TransactionStatsCollector transactionStatsCollector) {
            return new DefaultTransactionMonitoringService(transactionStatsCollector);
        }

        @Bean
        public TransactionStatsCollector transactionStatsCollector() {
            return new DefaultTransactionStatsCollector(30);
        }

        @Bean
        public TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor(
                TransactionStatsCollector transactionStatsCollector,
                TransactionAccessor transactionAccessor,
                ObjectProvider<AxelixMetricsPublisher> axelixMetricsPublisherObjectProvider) {

            return new TransactionMonitoringBeanPostProcessor(
                    transactionStatsCollector, axelixMetricsPublisherObjectProvider, transactionAccessor);
        }

        @Bean
        public ProxyingDataSourceBeanPostProcessor transactionMonitoringDataSourceBeanPostProcessor(
                TransactionAccessor transactionAccessor) {
            return new ProxyingDataSourceBeanPostProcessor(transactionAccessor);
        }

        @Bean
        public PropagationTestHelper propagationTestHelper(
                OwnerRepository ownerRepository, PetRepository petRepository, @Lazy PropagationTestHelper self) {
            return new PropagationTestHelper(ownerRepository, petRepository, self);
        }

        @Bean
        public PropagationTestService propagationTestService(
                OwnerRepository ownerRepository, PropagationTestHelper helper) {
            return new PropagationTestService(ownerRepository, helper);
        }

        @Bean
        public AxelixMetricsPublisher axelixMetricsPublisher(MeterRegistry meterRegistry) {
            return new DefaultAxelixMetricsPublisher(meterRegistry);
        }

        @Bean
        public TransactionAccessor transactionAccessor() {
            return new TransactionAccessor();
        }

        @EventListener(ApplicationReadyEvent.class)
        public void registerAppender() {
            new LogbackInMemoryPaginationAppenderRegistrar().register();
        }

        @Bean
        public HibernatePropertiesCustomizer axelixhibernatePropertiesCustomizer(
                TransactionAccessor transactionAccessor) {
            return properties ->
                    properties.put(INTEGRATOR_PROVIDER, (IntegratorProvider) () -> List.of(new NPlusOneIntegrator(
                            new NPlusOneEntityLoadListener(transactionAccessor),
                            new NPlusOneCollectionLoadListener(transactionAccessor))));
        }
    }

    @Entity
    @Table(name = "owner")
    static class Owner {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String lastName;

        @OneToMany(mappedBy = "owner", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
        private List<Pet> pets = new ArrayList<>();

        @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @BatchSize(size = 2)
        private List<Tag> tags = new ArrayList<>();

        public List<Pet> getPets() {
            return pets;
        }

        public Long getId() {
            return id;
        }

        public String getLastName() {
            return lastName;
        }

        public Owner setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Owner addPet(Pet pet) {
            this.pets.add(pet);
            return this;
        }

        public List<Tag> getTags() {
            return tags;
        }

        public void addTag(Tag tag) {
            this.tags.add(tag);
        }
    }

    @Entity
    @Table(name = "pet")
    static class Pet {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @ManyToOne
        @JoinColumn(name = "owner_id")
        private Owner owner;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "category_id")
        private Category category;

        public Pet() {}

        public Pet(String name, Owner owner) {
            this.name = name;
            this.owner = owner;
        }

        public Pet(String name, Owner owner, Category category) {
            this.name = name;
            this.owner = owner;
            this.category = category;
        }

        public Owner getOwner() {
            return owner;
        }

        public Category getCategory() {
            return category;
        }
    }

    @Entity
    @Table(name = "tag")
    static class Tag {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @ManyToOne
        @JoinColumn(name = "owner_id")
        @OnDelete(action = OnDeleteAction.CASCADE)
        private Owner owner;

        public Tag() {}

        public Tag(String name, Owner owner) {
            this.name = name;
            this.owner = owner;
        }
    }

    @Entity
    @Table(name = "category")
    @BatchSize(size = 2)
    static class Category {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        public Category() {}

        public Category(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    interface OwnerRepository extends JpaRepository<Owner, Long> {

        @Transactional
        Owner findByLastName(String lastName);

        //        @Transactional(propagation = Propagation.SUPPORTS)
        //        default List<Owner> findAll() {
        //            return List.of(new Owner());
        //        }

        @Transactional
        @Query(
                value = "SELECT o FROM AbstractTransactionMonitoringSharedContextTest$Owner o JOIN FETCH o.pets",
                countQuery = "SELECT COUNT(o) FROM AbstractTransactionMonitoringSharedContextTest$Owner o")
        Page<Owner> findAllWithPets(Pageable pageable);
    }

    interface PetRepository extends JpaRepository<Pet, Long> {}

    interface CategoryRepository extends JpaRepository<Category, Long> {}

    static class PropagationTestHelper {

        private final OwnerRepository ownerRepository;
        private final PropagationTestHelper self;
        private final PetRepository petRepository;

        public PropagationTestHelper(
                OwnerRepository ownerRepository, PetRepository petRepository, @Lazy PropagationTestHelper self) {
            this.ownerRepository = ownerRepository;
            this.petRepository = petRepository;
            this.self = self;
        }

        // IMPORTANT: Calling via 'self' proxy is required to properly test the REQUIRED -> REQUIRES_NEW stack behavior.
        @Transactional(propagation = Propagation.REQUIRED)
        public void outerRequiredMethod(String outerName) {
            ownerRepository.save(new Owner().setLastName(outerName));

            self.saveRequiresNew("SomeName");

            ownerRepository.save(new Owner().setLastName(outerName));
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void saveRequiresNew(String lastName) {
            ownerRepository.save(new Owner().setLastName(lastName));
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void testSaveMultipleOwners() {
            ownerRepository.saveAll(List.of(new Owner(), new Owner(), new Owner()));
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void updateOwner(Owner owner) {
            // will cause entityManager.merge --> new SELECT, since Owner has an id
            ownerRepository.save(owner);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void findOwnerById(Long id) {
            Owner owner = ownerRepository.findById(id).orElseThrow();
            owner.getPets().size(); // will cause n + 1
        }

        @Transactional(propagation = Propagation.NESTED)
        public void testNested() {
            ownerRepository.findByLastName("Schroeder");
        }

        @Transactional(propagation = Propagation.SUPPORTS)
        public void testSupports(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional(propagation = Propagation.SUPPORTS)
        public void testSupportsWithoutTransaction() {}

        @Transactional
        public void testRollbackScenario(String lastName) {
            ownerRepository.findByLastName(lastName);
            throw new RuntimeException("Test rollback");
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void findAllWithPetsPageable() {
            ownerRepository.findAllWithPets(PageRequest.of(0, 5));
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void testRequiresNew(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void testNestedRequiresNew() {
            ownerRepository.findByLastName("Franklin");
        }

        @Transactional(propagation = Propagation.MANDATORY)
        public void testMandatory(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void loadOwnersAndAccessPets() {
            List<Owner> owners = ownerRepository.findAll();
            owners.forEach(o -> o.getPets().size());
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void loadOwnersAndAccessTags() {
            List<Owner> owners = ownerRepository.findAll();
            owners.forEach(o -> o.getTags().size());
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void loadPetsAndAccessOwners() {
            List<Pet> pets = petRepository.findAll();
            pets.forEach(p -> p.getOwner().getId());
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void loadPetsAndAccessCategories() {
            List<Pet> pets = petRepository.findAll();
            pets.forEach(p -> p.getCategory().getName());
        }
    }

    static class PropagationTestService {

        private final OwnerRepository ownerRepository;
        private final PropagationTestHelper helperService;

        public PropagationTestService(OwnerRepository ownerRepository, PropagationTestHelper helperService) {
            this.ownerRepository = ownerRepository;
            this.helperService = helperService;
        }

        @Transactional(propagation = Propagation.REQUIRED)
        void testRequired(String lastName) {
            ownerRepository.findByLastName(lastName);
            helperService.testNestedRequiresNew();
        }
    }
}
