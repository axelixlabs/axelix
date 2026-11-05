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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.PlatformTransactionManager;

import com.axelixlabs.axelix.common.contract.metadata.InstanceFeature;
import com.axelixlabs.axelix.common.contract.metadata.JpaEntities;
import com.axelixlabs.axelix.common.contract.metadata.MappedEntity;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.JpaEntitiesProfileProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.NoOpJpaEntitiesProfileProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link DefaultInstanceFeaturesDetector}.
 *
 * @author Mikhail Polivakha
 */
class DefaultInstanceFeaturesDetectorTest {

    @Test
    void detectsOnlyDependencyAnalysis_whenNoInfrastructureIsPresent() {
        // given.
        var subject = createSubject(new DefaultListableBeanFactory(), new NoOpJpaEntitiesProfileProvider());

        // when.
        Set<InstanceFeature> features = subject.detect();

        // then.
        assertThat(features).containsExactly(InstanceFeature.DEPENDENCY_ANALYSIS);
    }

    @Test
    void detectsCaches_whenCacheManagerIsPresent() {
        // given.
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("cacheManager", new ConcurrentMapCacheManager());
        var subject = createSubject(beanFactory, new NoOpJpaEntitiesProfileProvider());

        // when.
        Set<InstanceFeature> features = subject.detect();

        // then.
        assertThat(features).contains(InstanceFeature.CACHES);
    }

    @Test
    void detectsScheduledTasks_whenTaskIsScheduled() {
        // given.
        ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();
        registrar.setScheduler(new ConcurrentTaskScheduler());
        registrar.addCronTask(() -> {}, "0 0 0 * * *");
        registrar.afterPropertiesSet();

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("scheduledTaskRegistrar", registrar);
        var subject = createSubject(beanFactory, new NoOpJpaEntitiesProfileProvider());

        try {
            // when.
            Set<InstanceFeature> features = subject.detect();

            // then.
            assertThat(features).contains(InstanceFeature.SCHEDULED_TASKS);
        } finally {
            registrar.destroy();
        }
    }

    @Test
    void doesNotDetectScheduledTasks_whenSchedulingHasNoTasks() {
        // given.
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("scheduledTaskRegistrar", new ScheduledTaskRegistrar());
        var subject = createSubject(beanFactory, new NoOpJpaEntitiesProfileProvider());

        // when.
        Set<InstanceFeature> features = subject.detect();

        // then.
        assertThat(features).doesNotContain(InstanceFeature.SCHEDULED_TASKS);
    }

    @Test
    void detectsEntitiesMap_whenJpaEntitiesArePresent() {
        // given.
        JpaEntitiesProfileProvider entitiesProvider = () -> new JpaEntities()
                .entities(List.of(new MappedEntity().name("Owner").table("owners")));
        var subject = createSubject(new DefaultListableBeanFactory(), entitiesProvider);

        // when.
        Set<InstanceFeature> features = subject.detect();

        // then.
        assertThat(features).contains(InstanceFeature.ENTITIES_MAP);
    }

    @Test
    void detectsTransactions_whenTransactionManagerIsPresent() {
        // given.
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("transactionManager", mock(PlatformTransactionManager.class));
        var subject = createSubject(beanFactory, new NoOpJpaEntitiesProfileProvider());

        // when.
        Set<InstanceFeature> features = subject.detect();

        // then.
        assertThat(features).contains(InstanceFeature.TRANSACTIONS);
    }

    private static DefaultInstanceFeaturesDetector createSubject(
            DefaultListableBeanFactory beanFactory, JpaEntitiesProfileProvider entitiesProvider) {
        return new DefaultInstanceFeaturesDetector(beanFactory, entitiesProvider);
    }
}
