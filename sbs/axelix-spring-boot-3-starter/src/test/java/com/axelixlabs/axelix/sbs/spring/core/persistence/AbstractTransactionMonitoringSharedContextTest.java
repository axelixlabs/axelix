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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.metrics.DefaultAxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneCollectionLoadListener;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneEntityLoadListener;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneIntegrator;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.pagination.LogbackInMemoryPaginationAppenderRegistrar;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.DefaultTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAttributesRegistry;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

import static org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.INTEGRATOR_PROVIDER;

/**
 * Base Spring Boot Test context class for the transaction-monitoring integration tests.
 * It owns the {@link SpringBootTest} declaration and the shared {@link TestConfiguration},
 * so that all subclasses resolve to an identical merged context configuration and therefore share a
 * single cached {@link org.springframework.context.ApplicationContext}.
 * <p>
 * <strong>All</strong> test configurations and fixtures consumed by the transaction-monitoring tests
 * live here, in the parent, as nested classes — so the parent owns the entire shared configuration and
 * never has to reference its subclasses.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
    AbstractTransactionMonitoringSharedContextTest.SharedTransactionTestConfiguration.class,
    JwtAuthTestConfiguration.class
})
abstract class AbstractTransactionMonitoringSharedContextTest {

    @TestConfiguration
    @EnableJpaRepositories(basePackageClasses = OwnerRepository.class, considerNestedRepositories = true)
    @EntityScan(basePackageClasses = {Owner.class, Pet.class})
    // Caching goes first, as it usually does: a cache hit then never reaches the transaction interceptor.
    @EnableCaching(order = Ordered.HIGHEST_PRECEDENCE)
    static class SharedTransactionTestConfiguration {

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("owners", "cachedService");
        }

        @Bean
        public TransactionStatsCollector transactionStatsCollector() {
            return new DefaultTransactionStatsCollector();
        }

        @Bean
        public TransactionAttributesRegistry transactionAttributesRegistry() {
            return new TransactionAttributesRegistry();
        }

        @Bean
        public TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor(
                TransactionStatsCollector transactionStatsCollector,
                TransactionAccessor transactionAccessor,
                TransactionAttributesRegistry transactionAttributesRegistry,
                ObjectProvider<AxelixMetricsPublisher> axelixMetricsPublisherObjectProvider,
                ApplicationContext applicationContext) {

            return new TransactionMonitoringBeanPostProcessor(
                    transactionStatsCollector,
                    axelixMetricsPublisherObjectProvider,
                    transactionAccessor,
                    transactionAttributesRegistry,
                    applicationContext);
        }

        @Bean
        public ProxyingDataSourceBeanPostProcessor transactionMonitoringDataSourceBeanPostProcessor(
                TransactionAccessor transactionAccessor) {
            return new ProxyingDataSourceBeanPostProcessor(transactionAccessor);
        }

        @Bean
        public OverloadedService overloadedService() {
            return new OverloadedService();
        }

        @Bean
        public DiamondService diamondService() {
            return new DiamondService();
        }

        @Bean
        public TransactionalOnInterface transactionalOnInterface() {
            return new TransactionalOnInterfaceImpl();
        }

        @Bean
        public TransactionalDefaultMethodOnlyImpl transactionalDefaultMethodOnlyImpl() {
            return new TransactionalDefaultMethodOnlyImpl();
        }

        @Bean
        public ConcreteFromAbstract concreteFromAbstract() {
            return new ConcreteFromAbstract();
        }

        @Bean
        public FirstImplementation firstImplementation() {
            return new FirstImplementation();
        }

        @Bean
        public SecondImplementation secondImplementation() {
            return new SecondImplementation();
        }

        @Bean
        public JdkProxiedService jdkProxiedService() {
            ProxyFactory factory = new ProxyFactory(new JdkProxiedServiceImpl());
            factory.setProxyTargetClass(false);
            factory.setInterfaces(JdkProxiedService.class);
            return (JdkProxiedService) factory.getProxy();
        }

        @Bean
        public ThrowingService throwingService() {
            return new ThrowingService();
        }

        @Bean
        public VisibilityService visibilityService() {
            return new VisibilityService();
        }

        @Bean
        public FinalMethodService finalMethodService() {
            return new FinalMethodService();
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
        public ScopedService scopedService() {
            return new ScopedService();
        }

        @Bean
        public ImplementationOnlyAnnotated implementationOnlyAnnotatedProxy() {
            ProxyFactory factory = new ProxyFactory(new ImplementationOnlyAnnotatedImpl());
            factory.setProxyTargetClass(false);
            factory.setInterfaces(ImplementationOnlyAnnotated.class);
            return (ImplementationOnlyAnnotated) factory.getProxy();
        }

        @Bean
        public BothAnnotatedImpl bothAnnotatedImpl() {
            return new BothAnnotatedImpl();
        }

        @Bean
        public ClassLevelConcrete classLevelConcrete() {
            return new ClassLevelConcrete();
        }

        @Bean
        public MarkedService multiInterfaceProxy() {
            ProxyFactory factory = new ProxyFactory(new MultiInterfaceImpl());
            factory.setProxyTargetClass(false);
            factory.setInterfaces(UnmarkedInterface.class, MarkedService.class);
            return (MarkedService) factory.getProxy();
        }

        @Bean
        public IntegerIdLookupImpl integerIdLookupImpl() {
            return new IntegerIdLookupImpl();
        }

        @Bean
        public ChildOverridesWithoutAnnotation childOverridesWithoutAnnotation() {
            return new ChildOverridesWithoutAnnotation();
        }

        @Bean
        public ClassLevelService classLevelService() {
            return new ClassLevelService();
        }

        @Bean
        public ClassLevelChildOfPlainParent classLevelChildOfPlainParent() {
            return new ClassLevelChildOfPlainParent();
        }

        @Bean
        public ClassLevelInterfaceImpl classLevelInterfaceImpl() {
            return new ClassLevelInterfaceImpl();
        }

        @Bean
        public ImplementsTransactionalAbstractMethod implementsTransactionalAbstractMethod() {
            return new ImplementsTransactionalAbstractMethod();
        }

        @Bean
        public ChildOverridesClassLevelParentMethod childOverridesClassLevelParentMethod() {
            return new ChildOverridesClassLevelParentMethod();
        }

        @Bean
        public ClassLevelWithMethodOverride classLevelWithMethodOverride() {
            return new ClassLevelWithMethodOverride();
        }

        @Bean
        public MetaAnnotatedService metaAnnotatedService() {
            return new MetaAnnotatedService();
        }

        @Bean
        public JakartaAnnotatedService jakartaAnnotatedService() {
            return new JakartaAnnotatedService();
        }

        @Bean
        public SelfInvokingService selfInvokingService() {
            return new SelfInvokingService();
        }

        @Bean
        public ChildInterfaceImpl childInterfaceImpl() {
            return new ChildInterfaceImpl();
        }

        @Bean
        public OverridesTransactionalDefaultMethod overridesTransactionalDefaultMethod() {
            return new OverridesTransactionalDefaultMethod();
        }

        @Bean
        public ImplementationOnlyAnnotatedImpl implementationOnlyAnnotatedImpl() {
            return new ImplementationOnlyAnnotatedImpl();
        }

        @Bean
        public ThreeLevelImplementation threeLevelImplementation() {
            return new ThreeLevelImplementation();
        }

        @Bean
        public SupportsOnlyService supportsOnlyService() {
            return new SupportsOnlyService();
        }

        @Bean
        public CachedService cachedService() {
            return new CachedService();
        }

        @Bean
        public FactoryBean<FactoryMadeService> factoryMadeService() {
            return new FactoryMadeServiceFactoryBean();
        }

        @Bean
        public JpaRepositoryFactoryBean<ManuallyRegisteredRepository, Category, Long> manuallyRegisteredRepository() {
            JpaRepositoryFactoryBean<ManuallyRegisteredRepository, Category, Long> factoryBean =
                    new JpaRepositoryFactoryBean<>(ManuallyRegisteredRepository.class);

            // Puts an advice into the repository proxy itself, ahead of its transaction interceptor: it answers
            // existsById() on its own, so that call never reaches the transaction interceptor.
            MethodInterceptor answeringExistsById = invocation ->
                    invocation.getMethod().getName().equals("existsById") ? Boolean.FALSE : invocation.proceed();
            factoryBean.addRepositoryFactoryCustomizer(factory -> factory.addRepositoryProxyPostProcessor(
                    (proxyFactory, repositoryInformation) -> proxyFactory.addAdvice(0, answeringExistsById)));

            return factoryBean;
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

        // @Cacheable makes Spring wrap the repository proxy into one more, caching, proxy.
        @Cacheable("owners")
        @Transactional
        Owner findByLastName(String lastName);

        @Transactional
        @Query(
                value = "SELECT o FROM AbstractTransactionMonitoringSharedContextTest$Owner o JOIN FETCH o.pets",
                countQuery = "SELECT COUNT(o) FROM AbstractTransactionMonitoringSharedContextTest$Owner o")
        Page<Owner> findAllWithPets(Pageable pageable);

        /**
         * Opens a transaction and runs several simple (non-N+1, non-in-memory-paginated) queries.
         */
        @Transactional
        default void executeMultipleSimpleQueries(String lastName) {
            save(new Owner().setLastName(lastName));
            findByLastName(lastName);
            count();
        }

        /**
         * Opens a transaction that performs a classic collection N+1 (load owners, then lazy-load pets).
         */
        @Transactional
        default void executeNPlusOneOnly() {
            List<Owner> owners = findAll();
            owners.forEach(owner -> owner.getPets().size());
        }

        /**
         * Opens a transaction with one extra simple query plus a collection N+1 on pets.
         */
        @Transactional
        default void executeNPlusOneAndSimpleQuery(String lastName) {
            findByLastName(lastName);
            List<Owner> owners = findAll();
            owners.forEach(owner -> owner.getPets().size());
        }

        /**
         * Opens a transaction with a simple query plus two different collection N+1s (pets and tags).
         */
        @Transactional
        default void executeTwoNPlusOnesAndSimpleQuery(String lastName) {
            findByLastName(lastName);
            List<Owner> owners = findAll();
            owners.forEach(owner -> {
                owner.getPets().size();
                owner.getTags().size();
            });
        }

        /**
         * Opens a transaction that triggers Hibernate in-memory pagination (JOIN FETCH + Pageable).
         */
        @Transactional
        default void executeInMemoryPagination() {
            findAllWithPets(PageRequest.of(0, 5));
        }

        /**
         * Opens a transaction with one simple query plus in-memory pagination.
         */
        @Transactional
        default void executeInMemoryPaginationAndSimpleQuery(String lastName) {
            findByLastName(lastName);
            findAllWithPets(PageRequest.of(0, 5));
        }

        /**
         * Opens a transaction with a simple query, a collection N+1, and in-memory pagination.
         *
         * <p>N+1 is executed before the JOIN FETCH page query so that pets are still lazy when accessed.
         */
        @Transactional
        default void executeInMemoryPaginationNPlusOneAndSimpleQuery(String lastName) {
            findByLastName(lastName);
            List<Owner> owners = findAll();
            owners.forEach(owner -> owner.getPets().size());
            findAllWithPets(PageRequest.of(0, 5));
        }
    }

    static class OverloadedService {
        public void process(String id) {}

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void process(String id, boolean force) {}
    }

    interface NonTransactionalDefaultMethod {
        default void process(String id) {}
    }

    interface TransactionalDefaultMethod {
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        default void process(String id, boolean force) {}
    }

    static class DiamondService implements NonTransactionalDefaultMethod, TransactionalDefaultMethod {}

    interface TransactionalOnInterface {
        @Transactional
        void doWork();
    }

    static class TransactionalOnInterfaceImpl implements TransactionalOnInterface {
        @Override
        public void doWork() {}
    }

    interface TransactionalDefaultMethodOnly {
        @Transactional
        default void doWork() {}
    }

    static class TransactionalDefaultMethodOnlyImpl implements TransactionalDefaultMethodOnly {}

    abstract static class AbstractTransactionalBase {
        @Transactional
        public void doWork() {}
    }

    static class ConcreteFromAbstract extends AbstractTransactionalBase {}

    interface TwoImplementations {
        @Transactional
        void doWork();
    }

    static class FirstImplementation implements TwoImplementations {
        @Override
        public void doWork() {}
    }

    static class SecondImplementation implements TwoImplementations {
        @Override
        public void doWork() {}
    }

    static class ThrowingService {
        @Transactional
        public void fail() {
            throw new IllegalStateException("boom");
        }
    }

    static class VisibilityService {
        @Transactional
        protected void protectedWork() {}

        @Transactional
        void packageWork() {}
    }

    static class FinalMethodService {
        @Transactional
        public final void work() {}
    }

    static class ScopedService {
        @Transactional
        public void work() {}
    }

    interface JdkProxiedService {
        @Transactional
        void doWork();
    }

    static class JdkProxiedServiceImpl implements JdkProxiedService {
        @Override
        public void doWork() {}
    }

    interface ImplementationOnlyAnnotated {
        void run();
    }

    static class ImplementationOnlyAnnotatedImpl implements ImplementationOnlyAnnotated {
        @Override
        @Transactional
        public void run() {}
    }

    interface BothAnnotated {
        @Transactional
        void doWork();
    }

    static class BothAnnotatedImpl implements BothAnnotated {
        @Override
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void doWork() {}
    }

    @Transactional(readOnly = true)
    abstract static class ClassLevelAbstractBase {
        public void inherited() {}
    }

    static class ClassLevelConcrete extends ClassLevelAbstractBase {}

    interface UnmarkedInterface {
        void mark();
    }

    interface MarkedService {
        @Transactional
        void doWork();
    }

    static class MultiInterfaceImpl implements UnmarkedInterface, MarkedService {
        @Override
        public void mark() {}

        @Override
        public void doWork() {}
    }

    interface IdLookup<T, ID> {
        T findById(ID id);
    }

    interface IntegerIdLookup extends IdLookup<Owner, Integer> {
        @Override
        @Transactional
        Owner findById(Integer id);
    }

    static class IntegerIdLookupImpl implements IntegerIdLookup {
        @Override
        public Owner findById(Integer id) {
            return null;
        }
    }

    static class ParentWithTransactionalMethod {
        @Transactional
        public void doWork() {}
    }

    static class ChildOverridesWithoutAnnotation extends ParentWithTransactionalMethod {
        @Override
        public void doWork() {}
    }

    @Transactional(readOnly = true)
    static class ClassLevelService {
        public void first() {}

        public void second() {}
    }

    static class ParentWithoutAnnotation {
        public void inherited() {}
    }

    @Transactional
    static class ClassLevelChildOfPlainParent extends ParentWithoutAnnotation {}

    abstract static class AbstractWithTransactionalAbstractMethod {
        @Transactional
        public abstract void doWork();
    }

    static class ImplementsTransactionalAbstractMethod extends AbstractWithTransactionalAbstractMethod {
        @Override
        public void doWork() {}
    }

    @Transactional
    static class ClassLevelParent {
        public void doWork() {}
    }

    static class ChildOverridesClassLevelParentMethod extends ClassLevelParent {
        @Override
        public void doWork() {}
    }

    @Transactional
    static class ClassLevelWithMethodOverride {
        public void classLevel() {}

        @Transactional(propagation = Propagation.SUPPORTS)
        public void methodLevel() {}
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(readOnly = true)
    @interface ReadOnlyTransactional {}

    static class MetaAnnotatedService {
        @ReadOnlyTransactional
        public void doWork() {}
    }

    static class JakartaAnnotatedService {
        @jakarta.transaction.Transactional
        public void doWork() {}
    }

    static class SelfInvokingService {
        public void outer() {
            inner();
        }

        @Transactional
        public void inner() {}
    }

    interface ParentInterface {
        @Transactional
        void doWork();
    }

    interface ChildInterface extends ParentInterface {}

    static class ChildInterfaceImpl implements ChildInterface {
        @Override
        public void doWork() {}
    }

    static class OverridesTransactionalDefaultMethod implements TransactionalDefaultMethodOnly {
        @Override
        public void doWork() {}
    }

    /**
     * Annotated on every level - interface, abstract class, implementation - each time with another propagation,
     * so that the propagation that ends up applied tells which level has won.
     */
    interface ThreeLevelContract {
        @Transactional(propagation = Propagation.SUPPORTS)
        void annotatedOnEveryLevel();

        @Transactional(propagation = Propagation.SUPPORTS)
        void annotatedOnInterfaceAndAbstractClass();

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        void annotatedOnInterfaceOnly();
    }

    abstract static class ThreeLevelAbstractClass implements ThreeLevelContract {
        @Override
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        public void annotatedOnEveryLevel() {}

        @Override
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void annotatedOnInterfaceAndAbstractClass() {}

        @Override
        public void annotatedOnInterfaceOnly() {}
    }

    static class ThreeLevelImplementation extends ThreeLevelAbstractClass {
        @Override
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void annotatedOnEveryLevel() {}

        @Override
        public void annotatedOnInterfaceAndAbstractClass() {}

        @Override
        public void annotatedOnInterfaceOnly() {}
    }

    static class SupportsOnlyService {
        @Transactional(propagation = Propagation.SUPPORTS)
        public void doWork() {}
    }

    static class CachedService {
        @Cacheable("cachedService")
        @Transactional
        public String load(String key) {
            return key;
        }
    }

    @Transactional
    interface ClassLevelInterface {
        void work();
    }

    static class ClassLevelInterfaceImpl implements ClassLevelInterface {
        @Override
        public void work() {}
    }

    interface PetRepository extends JpaRepository<Pet, Long> {}

    interface CategoryRepository extends JpaRepository<Category, Long> {

        @Override
        Optional<Category> findById(Long id);

        @Override
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        List<Category> findAll();
    }

    @Transactional(readOnly = true)
    interface TransactionalCategoryRepository extends JpaRepository<Category, Long> {

        List<Category> findByName(String name);
    }

    /** Kept away from repository scanning, to be registered by hand through its factory bean. */
    @NoRepositoryBean
    interface ManuallyRegisteredRepository extends JpaRepository<Category, Long> {}

    interface FactoryMadeService {
        @Transactional
        void doWork();
    }

    static class FactoryMadeServiceImpl implements FactoryMadeService {
        @Override
        public void doWork() {}
    }

    static class FactoryMadeServiceFactoryBean implements FactoryBean<FactoryMadeService> {
        @Override
        public FactoryMadeService getObject() {
            return new FactoryMadeServiceImpl();
        }

        @Override
        public Class<?> getObjectType() {
            return FactoryMadeService.class;
        }
    }

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
