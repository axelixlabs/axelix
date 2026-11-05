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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.util.ClassUtils;

import com.axelixlabs.axelix.common.contract.metadata.InstanceFeature;
import com.axelixlabs.axelix.common.contract.metadata.MappedEntity;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.JpaEntitiesProfileProvider;

/**
 * Default {@link InstanceFeaturesDetector} based on the infrastructure beans of the application context.
 *
 * @author Mikhail Polivakha
 */
public class DefaultInstanceFeaturesDetector implements InstanceFeaturesDetector {

    private static final ClassLoader CLASS_LOADER = DefaultInstanceFeaturesDetector.class.getClassLoader();

    // spring-tx is not guaranteed to be on the classpath, hence the class name rather than the class literal
    private static final String TRANSACTION_MANAGER_CLASS_NAME =
            "org.springframework.transaction.PlatformTransactionManager";

    private final ListableBeanFactory beanFactory;
    private final JpaEntitiesProfileProvider jpaEntitiesProfileProvider;

    public DefaultInstanceFeaturesDetector(
            ListableBeanFactory beanFactory, JpaEntitiesProfileProvider jpaEntitiesProfileProvider) {
        this.beanFactory = beanFactory;
        this.jpaEntitiesProfileProvider = jpaEntitiesProfileProvider;
    }

    @Override
    public Set<InstanceFeature> detect() {
        // The dependencies come from the build plugin, which every managed instance is required to apply
        Set<InstanceFeature> features = EnumSet.of(InstanceFeature.DEPENDENCY_ANALYSIS);

        if (hasBeanOfType(CacheManager.class)) {
            features.add(InstanceFeature.CACHES);
        }
        if (hasScheduledTasks()) {
            features.add(InstanceFeature.SCHEDULED_TASKS);
        }
        if (hasJpaEntities()) {
            features.add(InstanceFeature.ENTITIES_MAP);
        }
        if (hasTransactionManager()) {
            features.add(InstanceFeature.TRANSACTIONS);
        }

        return features;
    }

    private boolean hasScheduledTasks() {
        return beanFactory.getBeansOfType(ScheduledTaskHolder.class, false, false).values().stream()
                .anyMatch(holder -> !holder.getScheduledTasks().isEmpty());
    }

    private boolean hasJpaEntities() {
        List<MappedEntity> entities = jpaEntitiesProfileProvider.getEntities().getEntities();
        return entities != null && !entities.isEmpty();
    }

    private boolean hasTransactionManager() {
        if (!ClassUtils.isPresent(TRANSACTION_MANAGER_CLASS_NAME, CLASS_LOADER)) {
            return false;
        }
        return hasBeanOfType(ClassUtils.resolveClassName(TRANSACTION_MANAGER_CLASS_NAME, CLASS_LOADER));
    }

    private boolean hasBeanOfType(Class<?> type) {
        return beanFactory.getBeanNamesForType(type, false, false).length > 0;
    }
}
