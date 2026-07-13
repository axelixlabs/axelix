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
package com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.InitializeCollectionEvent;
import org.hibernate.event.spi.InitializeCollectionEventListener;
import org.hibernate.persister.collection.CollectionPersister;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

/**
 * Listener for Hibernate collection initialization events.
 * <p>
 * This listener intercepts lazy collection loads (e.g., {@code @OneToMany}, {@code @ManyToMany})
 * and records the collection role and owner ID for N+1 detection.
 * <p>
 * <b>Note:</b> This event is <b><i>only</i></b> triggered by a SELECT query.
 *
 * @author Nikita Kirillov
 */
public class NPlusOneCollectionLoadListener implements InitializeCollectionEventListener {

    private static final Logger log = LoggerFactory.getLogger(NPlusOneCollectionLoadListener.class);

    private final TransactionAccessor transactionAccessor;

    public NPlusOneCollectionLoadListener(TransactionAccessor transactionAccessor) {
        this.transactionAccessor = transactionAccessor;
    }

    @Override
    public void onInitializeCollection(InitializeCollectionEvent event) {
        try {
            PersistentCollection persistentCollection = event.getCollection();
            EventSource eventSource = event.getSession();
            CollectionPersister collectionPersister = eventSource
                    .getPersistenceContextInternal()
                    .getCollectionEntry(persistentCollection)
                    .getLoadedPersister();

            LazyLoadingTarget lazyLoadingTarget = parseLazyLoadingTarget(collectionPersister.getRole());

            if (lazyLoadingTarget != null) {
                transactionAccessor.recordLazyLoading(lazyLoadingTarget);
            }
        } catch (Exception ignored) {
        }
    }

    // The general "role" format is expected to look like this: com.example.Order.items
    public static @Nullable LazyLoadingTarget parseLazyLoadingTarget(String role) {
        try {
            int separatorIndex = role.lastIndexOf(".");
            Class<?> ownerEntityClass = Class.forName(role.substring(0, separatorIndex));
            String propertyName = role.substring(separatorIndex + 1);
            return new LazyLoadingTarget(ownerEntityClass, propertyName);
        } catch (ClassNotFoundException | IndexOutOfBoundsException e) {
            log.warn(
                    "Unexpected propertyPath format '{}'. Axelix cannot recognize that, so lazy loading and potential N + 1 is not going to be tracked for this property",
                    role);
            return null; // it means that the role format is not the one that we expect
        }
    }
}
