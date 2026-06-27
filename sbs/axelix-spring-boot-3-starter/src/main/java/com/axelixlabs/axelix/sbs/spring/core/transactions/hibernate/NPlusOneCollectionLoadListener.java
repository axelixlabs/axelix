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
package com.axelixlabs.axelix.sbs.spring.core.transactions.hibernate;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.InitializeCollectionEvent;
import org.hibernate.event.spi.InitializeCollectionEventListener;
import org.hibernate.persister.collection.CollectionPersister;

/**
 * Listener for Hibernate collection initialization events.
 * <p>
 * This listener intercepts lazy collection loads (e.g., {@code @OneToMany}, {@code @ManyToMany})
 * and records the collection role and owner ID for N+1 detection.
 * <p>
 * <b>Note:</b> This event is <b><i>only</i></b> triggered by a SELECT query.
 *
 * @see NPlusOneHolder#recordCollectionLoad(String, Object, boolean)
 * @author Nikita Kirillov
 */
public class NPlusOneCollectionLoadListener implements InitializeCollectionEventListener {

    private final NPlusOneHolder nPlusOneHolder;

    public NPlusOneCollectionLoadListener(NPlusOneHolder nPlusOneHolder) {
        this.nPlusOneHolder = nPlusOneHolder;
    }

    @Override
    public void onInitializeCollection(InitializeCollectionEvent event) {
        try {
            PersistentCollection<?> persistentCollection = event.getCollection();
            EventSource eventSource = event.getSession();
            CollectionPersister collectionPersister = eventSource
                    .getPersistenceContextInternal()
                    .getCollectionEntry(persistentCollection)
                    .getLoadedPersister();

            String role = collectionPersister.getRole();
            Object ownerId = event.getAffectedOwnerIdOrNull();
            boolean isBatch = collectionPersister.getBatchSize() > 1;
            if (ownerId != null) {
                nPlusOneHolder.recordCollectionLoad(role, ownerId, isBatch);
            }
        } catch (Exception ignored) {
        }
    }
}
