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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.IllegalTransactionStateException;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricNames;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LazyLoadingTarget;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAttributesRegistry;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStats;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end tests for {@link TransactionMonitoringInterceptor}. Aims to test the behavior
 * of Axelix detecting various problems during persistence, such as N + 1 and so on.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
class TransactionMonitoringInterceptorTest extends AbstractTransactionMonitoringSharedContextTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private TransactionStatsCollector transactionStatsCollector;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private PropagationTestHelper propagationTestHelper;

    @Autowired
    private OverloadedService overloadedService;

    @Autowired
    private DiamondService diamondService;

    @Autowired
    private TransactionalOnInterfaceImpl transactionalOnInterfaceImpl;

    @Autowired
    private TransactionalDefaultMethodOnlyImpl transactionalDefaultMethodOnlyImpl;

    @Autowired
    private ConcreteFromAbstract concreteFromAbstract;

    @Autowired
    private FirstImplementation firstImplementation;

    @Autowired
    private SecondImplementation secondImplementation;

    @Autowired
    private JdkProxiedService jdkProxiedService;

    @Autowired
    private MarkedService multiInterfaceProxy;

    @Autowired
    private ImplementationOnlyAnnotated implementationOnlyAnnotatedProxy;

    @Autowired
    private BothAnnotatedImpl bothAnnotatedImpl;

    @Autowired
    private ClassLevelConcrete classLevelConcrete;

    @Autowired
    private TransactionAttributesRegistry transactionAttributesRegistry;

    @Autowired
    private ThrowingService throwingService;

    @Autowired
    private VisibilityService visibilityService;

    @Autowired
    private FinalMethodService finalMethodService;

    @Autowired
    private ScopedService scopedService;

    @Autowired
    private IntegerIdLookupImpl integerIdLookupImpl;

    @Autowired
    private ChildOverridesWithoutAnnotation childOverridesWithoutAnnotation;

    @Autowired
    private ClassLevelService classLevelService;

    @Autowired
    private ClassLevelInterfaceImpl classLevelInterfaceImpl;

    @Autowired
    private ClassLevelChildOfPlainParent classLevelChildOfPlainParent;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionalCategoryRepository transactionalCategoryRepository;

    @Autowired
    private PropagationTestService propagationTestService;

    @Autowired
    private ImplementsTransactionalAbstractMethod implementsTransactionalAbstractMethod;

    @Autowired
    private ChildOverridesClassLevelParentMethod childOverridesClassLevelParentMethod;

    @Autowired
    private ClassLevelWithMethodOverride classLevelWithMethodOverride;

    @Autowired
    private MetaAnnotatedService metaAnnotatedService;

    @Autowired
    private JavaxAnnotatedService javaxAnnotatedService;

    @Autowired
    private SelfInvokingService selfInvokingService;

    @Autowired
    private ChildInterfaceImpl childInterfaceImpl;

    @Autowired
    private OverridesTransactionalDefaultMethod overridesTransactionalDefaultMethod;

    @Autowired
    private ImplementationOnlyAnnotatedImpl implementationOnlyAnnotatedImpl;

    @Autowired
    private CachedService cachedService;

    @Autowired
    private ManuallyRegisteredRepository manuallyRegisteredRepository;

    @Autowired
    private FactoryMadeService factoryMadeService;

    @Autowired
    private ThreeLevelImplementation threeLevelImplementation;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        petRepository.deleteAll();
        ownerRepository.deleteAll();

        Owner first = new Owner().setLastName("Davis");
        first.addPet(new Pet("Basil", first));
        first.addTag(new Tag("friendly", first));

        Owner second = new Owner().setLastName("Carter");
        second.addPet(new Pet("Leo", second));
        second.addTag(new Tag("trained", second));

        ownerRepository.saveAll(List.of(first, second));
        transactionStatsCollector.clear();
    }

    @Nested
    class SimpleQueries {

        @Test
        void shouldRecordTransactionWithMultipleSimpleQueries() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeMultipleSimpleQueries", String.class);

            // when.
            ownerRepository.executeMultipleSimpleQueries("Evans");

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).isEmpty();
            assertThat(stats.getInMemoryPaginatedEntities()).isEmpty();
            // INSERT owner + findByLastName + count.
            assertMetersRecordedFor("executeMultipleSimpleQueries", 3);
        }
    }

    /**
     * Methods inherited from {@code CrudRepository}/{@code JpaRepository} without being redeclared on the
     * repository interface: their {@code @Transactional} lives on the shared generic implementation
     * ({@link SimpleJpaRepository}), yet they are attributed to the repository interface they were called on.
     */
    @Nested
    class InheritedRepositoryMethods {

        @Test
        void shouldRecordTransactionForDirectSaveCall() throws Exception {
            // given.
            MethodClassKey key = new MethodClassKey(
                    SimpleJpaRepository.class.getMethod("save", Object.class), OwnerRepository.class);
            long recordedBefore = recordedTransactions(OwnerRepository.class, "save");

            // when.
            ownerRepository.save(new Owner().setLastName("Evans"));

            // then.
            assertThat(transactionStatsCollector.getCopyOfStats()).containsKey(key);
            assertThat(recordedTransactions(OwnerRepository.class, "save")).isEqualTo(recordedBefore + 1);
        }

        @Test
        void shouldRecordTransactionForDirectFindByIdCall() throws Exception {
            // given.
            Owner saved = ownerRepository.save(new Owner().setLastName("Evans"));
            transactionStatsCollector.clear();
            MethodClassKey key = new MethodClassKey(
                    SimpleJpaRepository.class.getMethod("findById", Object.class), OwnerRepository.class);

            // when.
            ownerRepository.findById(saved.getId());

            // then.
            assertThat(transactionStatsCollector.getCopyOfStats()).containsKey(key);
        }

        @Test
        void shouldRecordTransactionForDirectDeleteByIdCall() throws Exception {
            // given.
            Owner saved = ownerRepository.save(new Owner().setLastName("Evans"));
            transactionStatsCollector.clear();
            MethodClassKey key = new MethodClassKey(
                    SimpleJpaRepository.class.getMethod("deleteById", Object.class), OwnerRepository.class);

            // when.
            ownerRepository.deleteById(saved.getId());

            // then.
            assertThat(transactionStatsCollector.getCopyOfStats()).containsKey(key);
        }

        @Test
        void shouldAttributeSameInheritedMethodToEachRepositorySeparately() {
            // when.
            ownerRepository.count();
            petRepository.count();

            // then.
            assertThat(transactionStatsCollector.getCopyOfStats()).hasSize(2);
            assertRecorded(OwnerRepository.class, "count");
            assertRecorded(PetRepository.class, "count");
        }

        @Test
        void shouldRecordMethodCalledThroughBaseRepositoryInterface() {
            CrudRepository<Pet, Long> crudRepository = petRepository;

            // when.
            crudRepository.findAll();

            // then.
            assertThat(transactionStatsCollector.getCopyOfStats()).hasSize(1);
            assertRecorded(PetRepository.class, "findAll");
        }

        @Test
        void shouldRecordTransactionForRedeclaredInterfaceMethod() throws Exception {
            // given.
            Method findByLastName = OwnerRepository.class.getMethod("findByLastName", String.class);
            MethodClassKey key = new MethodClassKey(findByLastName, OwnerRepository.class);

            // when.
            ownerRepository.findByLastName("Davis");

            // then.
            assertThat(transactionStatsCollector.getCopyOfStats()).containsKey(key);
        }
    }

    /**
     * Methods declared on the repository interface itself, be it a redeclared inherited one or a query method.
     */
    @Nested
    class RepositoryInterfaceMethods {

        @Test
        void shouldRecordMethodRedeclaredWithConcreteIdType() {
            categoryRepository.findById(1L);

            assertRecorded(CategoryRepository.class, "findById", Long.class);
        }

        @Test
        void shouldNotRecordMethodRedeclaredAsNotSupported() {
            categoryRepository.findAll();

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }

        /**
         * A query method has no implementation to inherit a transaction from: it is transactional only because
         * the repository interface is annotated.
         */
        @Test
        void shouldRecordQueryMethodOfRepositoryAnnotatedOnTypeLevel() {
            transactionalCategoryRepository.findByName("Cats");

            assertRecorded(TransactionalCategoryRepository.class, "findByName", String.class);
        }
    }

    @Nested
    class FactoryBeanProducts {

        /**
         * Registered by hand through its factory bean rather than found by repository scanning.
         */
        @Test
        void shouldAttributeManuallyRegisteredRepositoryToItsInterface() {
            manuallyRegisteredRepository.count();

            assertRecorded(ManuallyRegisteredRepository.class, "count");
        }

        /**
         * The factory declares its product as an interface, yet there is a real implementation behind it.
         */
        @Test
        void shouldAttributeFactoryMadeServiceToItsImplementation() {
            factoryMadeService.doWork();

            assertRecorded(FactoryMadeServiceImpl.class, "doWork");
        }
    }

    /**
     * A call answered by some other advice before it ever reaches the transaction interceptor.
     */
    @Nested
    class CallsNotReachingTransactionInterceptor {

        /**
         * The answering advice sits in the repository proxy itself, right where the transaction interceptor
         * is - so this only holds if monitoring is placed after that advice, not at the head of the chain.
         */
        @Test
        void shouldNotRecordRepositoryCallAnsweredAheadOfTransactionInterceptor() {
            manuallyRegisteredRepository.existsById(1L);

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }

        @Test
        void shouldNotRecordCacheHitOfRepositoryMethod() {
            ownerRepository.findByLastName("CacheHit");
            assertRecorded(OwnerRepository.class, "findByLastName", String.class);
            transactionStatsCollector.clear();

            ownerRepository.findByLastName("CacheHit");

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }

        @Test
        void shouldNotRecordCacheHitOfServiceMethod() {
            cachedService.load("CacheHit");
            assertRecorded(CachedService.class, "load", String.class);
            transactionStatsCollector.clear();

            cachedService.load("CacheHit");

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }
    }

    @Nested
    class AnnotationOnClass {

        @Test
        void shouldNotRecordNonTransactionalOverload() {
            overloadedService.process("abc");

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }

        @Test
        void shouldRecordOnlyTransactionalOverload() {
            overloadedService.process("abc", true);

            assertRecorded(OverloadedService.class, "process", String.class, boolean.class);
            assertNotRecorded(OverloadedService.class, "process", String.class);
        }

        @Test
        void shouldRecordEveryMethodOfClassAnnotatedOnTypeLevel() {
            classLevelService.first();
            classLevelService.second();

            assertRecorded(ClassLevelService.class, "first");
            assertRecorded(ClassLevelService.class, "second");
        }

        @Test
        void shouldPreferMethodAnnotationOverTypeLevelOne() {
            classLevelWithMethodOverride.classLevel();
            classLevelWithMethodOverride.methodLevel();

            assertRecorded(ClassLevelWithMethodOverride.class, "classLevel");
            // SUPPORTS on the method itself wins over REQUIRED on the class.
            assertNotRecorded(ClassLevelWithMethodOverride.class, "methodLevel");
        }

        @Test
        void shouldRecordMethodInheritedFromAbstractClass() {
            concreteFromAbstract.doWork();

            assertRecorded(ConcreteFromAbstract.class, "doWork");
        }

        @Test
        void shouldRecordImplementationOfTransactionalAbstractMethod() {
            implementsTransactionalAbstractMethod.doWork();

            assertRecorded(ImplementsTransactionalAbstractMethod.class, "doWork");
        }

        @Test
        void shouldRecordMethodOverriddenWithoutAnnotation() {
            childOverridesWithoutAnnotation.doWork();

            assertRecorded(ChildOverridesWithoutAnnotation.class, "doWork");
        }

        @Test
        void shouldRecordMethodInheritedFromParentAnnotatedOnTypeLevel() {
            classLevelConcrete.inherited();

            assertRecorded(ClassLevelConcrete.class, "inherited");
        }

        @Test
        void shouldRecordOverrideOfMethodOfParentAnnotatedOnTypeLevel() {
            childOverridesClassLevelParentMethod.doWork();

            assertRecorded(ChildOverridesClassLevelParentMethod.class, "doWork");
        }

        /**
         * Spring does not apply a type-level annotation to a method inherited from a parent that lacks it.
         */
        @Test
        void shouldNotRecordMethodInheritedFromParentWithoutAnnotation() {
            classLevelChildOfPlainParent.inherited();

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }

        @Test
        void shouldRecordMethodAnnotatedWithComposedAnnotation() throws Exception {
            MethodClassKey key =
                    new MethodClassKey(MetaAnnotatedService.class.getMethod("doWork"), MetaAnnotatedService.class);

            metaAnnotatedService.doWork();

            assertRecorded(MetaAnnotatedService.class, "doWork");
            assertThat(transactionAttributesRegistry.get(key).isReadOnly()).isTrue();
        }

        @Test
        void shouldRecordMethodAnnotatedWithJavaxTransactional() {
            javaxAnnotatedService.doWork();

            assertRecorded(JavaxAnnotatedService.class, "doWork");
        }

        /**
         * Spring Framework 5 applies {@code @Transactional} to public methods only, so no transaction is opened
         * for these calls and none may be recorded.
         */
        @Test
        void shouldNotRecordProtectedAndPackagePrivateMethods() {
            visibilityService.protectedWork();
            visibilityService.packageWork();

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }

        /**
         * A final method cannot be overridden by the CGLIB proxy, so Spring opens no transaction for it.
         */
        @Test
        void shouldNotRecordFinalMethod() {
            finalMethodService.work();

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }

        /**
         * A call made on {@code this} bypasses the proxy, so Spring opens no transaction for it.
         */
        @Test
        void shouldNotRecordSelfInvocation() {
            selfInvokingService.outer();

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }
    }

    @Nested
    class AnnotationOnInterface {

        @Test
        void shouldRecordMethodAnnotatedOnInterface() {
            transactionalOnInterfaceImpl.doWork();

            assertRecorded(TransactionalOnInterfaceImpl.class, "doWork");
        }

        @Test
        void shouldRecordMethodAnnotatedOnParentInterface() {
            childInterfaceImpl.doWork();

            assertRecorded(ChildInterfaceImpl.class, "doWork");
        }

        @Test
        void shouldRecordMethodOfInterfaceAnnotatedOnTypeLevel() {
            classLevelInterfaceImpl.work();

            assertRecorded(ClassLevelInterfaceImpl.class, "work");
        }

        @Test
        void shouldRecordDefaultMethod() {
            transactionalDefaultMethodOnlyImpl.doWork();

            assertRecorded(TransactionalDefaultMethodOnlyImpl.class, "doWork");
        }

        @Test
        void shouldRecordOverrideOfDefaultMethod() {
            overridesTransactionalDefaultMethod.doWork();

            assertRecorded(OverridesTransactionalDefaultMethod.class, "doWork");
        }

        @Test
        void shouldNotRecordNonTransactionalDefaultMethodOfOtherInterface() {
            diamondService.process("abc");

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }

        @Test
        void shouldRecordTransactionalDefaultMethodOfOtherInterface() {
            diamondService.process("abc", true);

            assertRecorded(DiamondService.class, "process", String.class, boolean.class);
        }

        @Test
        void shouldAttributeEachOfTwoImplementationsSeparately() {
            firstImplementation.doWork();
            secondImplementation.doWork();

            assertThat(transactionStatsCollector.getCopyOfStats()).hasSize(2);
            assertRecorded(FirstImplementation.class, "doWork");
            assertRecorded(SecondImplementation.class, "doWork");
        }

        @Test
        void shouldRecordAnnotationOnlyOnImplementation() {
            implementationOnlyAnnotatedImpl.run();

            assertRecorded(ImplementationOnlyAnnotatedImpl.class, "run");
        }

        @Test
        void shouldUseImplementationAnnotationWhenBothInterfaceAndImplementationAreAnnotated() throws Exception {
            MethodClassKey key =
                    new MethodClassKey(BothAnnotatedImpl.class.getMethod("doWork"), BothAnnotatedImpl.class);

            bothAnnotatedImpl.doWork();

            assertRecorded(BothAnnotatedImpl.class, "doWork");
            assertThat(transactionAttributesRegistry.get(key).getPropagation()).isEqualTo("REQUIRES_NEW");
        }

        @Test
        void shouldRecordCovariantOverrideCalledDirectly() {
            integerIdLookupImpl.findById(1);

            assertRecorded(IntegerIdLookupImpl.class, "findById", Integer.class);
        }

        @Test
        void shouldRecordCovariantOverrideCalledThroughGenericInterface() {
            IdLookup<Owner, Integer> lookup = integerIdLookupImpl;

            lookup.findById(1);

            assertRecorded(IntegerIdLookupImpl.class, "findById", Integer.class);
        }
    }

    /**
     * A method annotated on several levels of the hierarchy at once - interface, abstract class, implementation.
     * The nearest annotation wins, and the transaction is attributed to the implementation.
     */
    @Nested
    class AnnotationOnSeveralLevels {

        @Test
        void shouldUseAnnotationOfImplementationWhenEveryLevelIsAnnotated() {
            threeLevelImplementation.annotatedOnEveryLevel();

            assertRecorded(ThreeLevelImplementation.class, "annotatedOnEveryLevel");
            assertThat(propagationOf("annotatedOnEveryLevel")).isEqualTo("REQUIRES_NEW");
        }

        @Test
        void shouldUseAnnotationOfAbstractClassWhenImplementationHasNone() {
            threeLevelImplementation.annotatedOnInterfaceAndAbstractClass();

            assertRecorded(ThreeLevelImplementation.class, "annotatedOnInterfaceAndAbstractClass");
            assertThat(propagationOf("annotatedOnInterfaceAndAbstractClass")).isEqualTo("REQUIRES_NEW");
        }

        @Test
        void shouldUseAnnotationOfInterfaceWhenNoClassHasOne() {
            threeLevelImplementation.annotatedOnInterfaceOnly();

            assertRecorded(ThreeLevelImplementation.class, "annotatedOnInterfaceOnly");
            assertThat(propagationOf("annotatedOnInterfaceOnly")).isEqualTo("REQUIRES_NEW");
        }

        private String propagationOf(String methodName) throws RuntimeException {
            try {
                MethodClassKey key = new MethodClassKey(
                        ThreeLevelImplementation.class.getMethod(methodName), ThreeLevelImplementation.class);
                return transactionAttributesRegistry.get(key).getPropagation();
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Beans that are JDK proxies built by hand, i.e. not by Spring's own auto-proxying.
     */
    @Nested
    class HandMadeJdkProxies {

        @Test
        void shouldAttributeToImplementationBehindTheProxy() {
            jdkProxiedService.doWork();

            assertRecorded(JdkProxiedServiceImpl.class, "doWork");
        }

        @Test
        void shouldAttributeToImplementationWhenProxyImplementsSeveralInterfaces() {
            multiInterfaceProxy.doWork();

            assertRecorded(MultiInterfaceImpl.class, "doWork");
        }

        /**
         * The proxy hides the implementation from Spring, so no transaction is ever opened.
         */
        @Test
        void shouldNotRecordAnnotationOnlyOnImplementationHiddenBehindTheProxy() {
            implementationOnlyAnnotatedProxy.run();

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }
    }

    @Nested
    class ScopedProxy {

        @Test
        void shouldRecordTransactionOfScopedBeanOnlyOnce() {
            long recordedBefore = recordedTransactions(ScopedService.class, "work");

            scopedService.work();

            assertRecorded(ScopedService.class, "work");
            assertThat(recordedTransactions(ScopedService.class, "work")).isEqualTo(recordedBefore + 1);
        }
    }

    @Nested
    class Propagations {

        @Test
        void shouldRecordOnlyOutermostRequiredAndEveryRequiresNew() {
            propagationTestService.testRequired("Davis");

            // The repository call inside joins the transaction of testRequired, so it is not a transaction on its own.
            assertThat(transactionStatsCollector.getCopyOfStats()).hasSize(2);
            assertRecorded(PropagationTestService.class, "testRequired", String.class);
            assertRecorded(PropagationTestHelper.class, "testNestedRequiresNew");
        }

        @Test
        void shouldRecordNestedWhenNoTransactionIsActive() {
            propagationTestHelper.testNested();

            assertThat(transactionStatsCollector.getCopyOfStats()).hasSize(1);
            assertRecorded(PropagationTestHelper.class, "testNested");
        }

        @Test
        void shouldNotRecordSupports() {
            propagationTestHelper.testSupportsWithoutTransaction();

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }

        @Test
        void shouldRecordTransactionOpenedInsideSupportsMethodButNotTheMethodItself() {
            propagationTestHelper.testSupports("Davis");

            assertThat(transactionStatsCollector.getCopyOfStats()).hasSize(1);
            assertRecorded(OwnerRepository.class, "findByLastName", String.class);
        }

        @Test
        void shouldNotRecordMandatoryRejectedForLackOfTransaction() {
            assertThatThrownBy(() -> propagationTestHelper.testMandatory("Davis"))
                    .isInstanceOf(IllegalTransactionStateException.class);

            assertThat(transactionStatsCollector.getCopyOfStats()).isEmpty();
        }
    }

    @Nested
    class ExceptionPath {

        @Test
        void shouldRecordTransactionWhenTransactionalMethodThrows() {
            assertThatThrownBy(() -> throwingService.fail()).isInstanceOf(IllegalStateException.class);

            assertRecorded(ThrowingService.class, "fail");
        }
    }

    @Nested
    class NPlusOne {

        @Test
        void shouldRecordTransactionWithOneNPlusOne() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeNPlusOneOnly");
            LazyLoadingTarget pets = new LazyLoadingTarget(Owner.class, "pets");

            // when.
            ownerRepository.executeNPlusOneOnly();

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).containsEntry(pets, 2); // two lazy loadings of pets
            assertThat(stats.getInMemoryPaginatedEntities()).isEmpty();
            // findAll owners + one lazy pets load per owner (2 owners).
            assertMetersRecordedFor("executeNPlusOneOnly", 3);
        }

        @Test
        void shouldRecordTransactionWithOneNPlusOneAndSimpleQuery() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeNPlusOneAndSimpleQuery", String.class);
            LazyLoadingTarget pets = new LazyLoadingTarget(Owner.class, "pets");

            // when.
            ownerRepository.executeNPlusOneAndSimpleQuery("Davis");

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).containsEntry(pets, 2);
            assertThat(stats.getInMemoryPaginatedEntities()).isEmpty();
            // findByLastName + findAll owners + one lazy pets load per owner (2 owners).
            assertMetersRecordedFor("executeNPlusOneAndSimpleQuery", 4);
        }

        @Test
        void shouldRecordTransactionWithTwoNPlusOnesAndSimpleQuery() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeTwoNPlusOnesAndSimpleQuery", String.class);
            LazyLoadingTarget pets = new LazyLoadingTarget(Owner.class, "pets");
            LazyLoadingTarget tags = new LazyLoadingTarget(Owner.class, "tags");

            // when.
            ownerRepository.executeTwoNPlusOnesAndSimpleQuery("Davis");

            // then.
            TransactionStats stats = statsFor(key);

            // Tags use @BatchSize, so they are loaded by a single batch load.
            assertThat(stats.getNPlusOneOccasions()).containsEntry(pets, 2).containsEntry(tags, 1);
            assertThat(stats.getInMemoryPaginatedEntities()).isEmpty();
            // findByLastName + findAll + one lazy pets load per owner (2) + a single batched tags load.
            assertMetersRecordedFor("executeTwoNPlusOnesAndSimpleQuery", 5);
        }
    }

    @Nested
    class InMemoryPagination {

        @Test
        void shouldRecordTransactionWithInMemoryPagination() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeInMemoryPagination");

            // when.
            ownerRepository.executeInMemoryPagination();

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).isEmpty();
            assertThat(stats.getInMemoryPaginatedEntities()).hasSize(1).containsEntry("owner", 1);
            // Single JOIN FETCH page query; the count query is skipped for page 0 that fits the results.
            assertMetersRecordedFor("executeInMemoryPagination", 1);
        }

        @Test
        void shouldRecordTransactionWithInMemoryPaginationAndSimpleQuery() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeInMemoryPaginationAndSimpleQuery", String.class);

            // when.
            ownerRepository.executeInMemoryPaginationAndSimpleQuery("Davis");

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).isEmpty();
            assertThat(stats.getInMemoryPaginatedEntities()).hasSize(1).containsEntry("owner", 1);
            // findByLastName + single JOIN FETCH page query (count query skipped).
            assertMetersRecordedFor("executeInMemoryPaginationAndSimpleQuery", 2);
        }

        @Test
        void shouldRecordTransactionWithInMemoryPaginationNPlusOneAndSimpleQuery() throws Exception {
            // given.
            MethodClassKey key = keyFor("executeInMemoryPaginationNPlusOneAndSimpleQuery", String.class);
            LazyLoadingTarget pets = new LazyLoadingTarget(Owner.class, "pets");

            // when.
            ownerRepository.executeInMemoryPaginationNPlusOneAndSimpleQuery("Davis");

            // then.
            TransactionStats stats = statsFor(key);
            assertThat(stats.getNPlusOneOccasions()).containsKey(pets);
            assertThat(stats.getInMemoryPaginatedEntities()).hasSize(1).containsEntry("owner", 1);
            // findByLastName + findAll + one lazy pets load per owner (2) + JOIN FETCH page query.
            assertMetersRecordedFor("executeInMemoryPaginationNPlusOneAndSimpleQuery", 5);
        }
    }

    @Nested
    class NestedTransactions {

        @Test
        void shouldIsolateQueriesBetweenOuterAndRequiresNewTransaction() throws Exception {
            // given.
            MethodClassKey outerKey = keyFor(PropagationTestHelper.class, "outerRequiredMethod", String.class);
            MethodClassKey innerKey = keyFor(PropagationTestHelper.class, "saveRequiresNew", String.class);

            // when.
            propagationTestHelper.outerRequiredMethod("Nested");

            // then.
            assertThat(transactionStatsCollector.getCopyOfStats()).containsKeys(outerKey, innerKey);
            assertMetersRecordedFor(PropagationTestHelper.class, "outerRequiredMethod", 2);
            assertMetersRecordedFor(PropagationTestHelper.class, "saveRequiresNew", 1);
        }
    }

    private TransactionStats statsFor(MethodClassKey key) {
        Map<MethodClassKey, TransactionStats> stats = transactionStatsCollector.getCopyOfStats();
        assertThat(stats).containsKey(key);
        return stats.get(key);
    }

    /**
     * Verifies that the transaction meters were published to the {@link MeterRegistry} for the given
     * repository method: a {@link Timer} recording the single monitored transaction and a {@link Counter}
     * holding the exact number of SQL queries executed inside it. These meters are exposed via the Axelix
     * actuator endpoint, so their tags and values form a contract worth asserting precisely.
     */
    private void assertMetersRecordedFor(String methodName, int expectedQueries) {
        assertMetersRecordedFor(OwnerRepository.class, methodName, expectedQueries);
    }

    private void assertMetersRecordedFor(Class<?> declaringClass, String methodName, int expectedQueries) {
        String className = declaringClass.getSimpleName();

        Timer durationTimer = meterRegistry
                .find(AxelixMetricNames.TRANSACTION_DURATION)
                .tag("class", className)
                .tag("method", methodName)
                .timer();
        assertThat(durationTimer).isNotNull();
        assertThat(durationTimer.count()).isEqualTo(1);
        assertThat(durationTimer.totalTime(TimeUnit.NANOSECONDS)).isPositive();

        Counter queriesCounter = meterRegistry
                .find(AxelixMetricNames.TRANSACTION_QUERIES)
                .tag("class", className)
                .tag("method", methodName)
                .counter();
        assertThat(queriesCounter).isNotNull();
        assertThat(queriesCounter.count()).isEqualTo(expectedQueries);
    }

    private long recordedTransactions(Class<?> identityClass, String methodName) {
        Timer timer = meterRegistry
                .find(AxelixMetricNames.TRANSACTION_DURATION)
                .tag("class", identityClass.getSimpleName())
                .tag("method", methodName)
                .timer();
        return timer == null ? 0 : timer.count();
    }

    private void assertRecorded(Class<?> identityClass, String methodName, Class<?>... parameterTypes) {
        assertThat(transactionStatsCollector.getCopyOfStats().keySet())
                .anyMatch(key -> key.getIdentityClass() == identityClass
                        && key.getMethod().getName().equals(methodName)
                        && Arrays.equals(key.getMethod().getParameterTypes(), parameterTypes));
    }

    private void assertNotRecorded(Class<?> identityClass, String methodName, Class<?>... parameterTypes) {
        assertThat(transactionStatsCollector.getCopyOfStats().keySet())
                .noneMatch(key -> key.getIdentityClass() == identityClass
                        && key.getMethod().getName().equals(methodName)
                        && Arrays.equals(key.getMethod().getParameterTypes(), parameterTypes));
    }

    private static MethodClassKey keyFor(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return keyFor(OwnerRepository.class, methodName, parameterTypes);
    }

    private static MethodClassKey keyFor(Class<?> declaringClass, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = declaringClass.getMethod(methodName, parameterTypes);
        return new MethodClassKey(method, declaringClass);
    }
}
