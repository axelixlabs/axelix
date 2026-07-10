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

import org.hibernate.event.spi.LoadEvent;
import org.hibernate.event.spi.LoadEventListener;

/**
 * Listener for Hibernate entity load events ({@code EventType.LOAD}).
 * <p>
 * Intercepts entity loads triggered by FK resolution — typically owning-side
 * {@code @ManyToOne} and {@code @OneToOne} associations where the FK is <b>on
 * the current entity's table</b>.
 * <p>
 * <b>Does not fire</b> for JOIN fetch, EntityGraph, or subselect loads —
 * those bypass {@code EventType.LOAD} entirely.
 * <p>
 *
 * @author Nikita Kirillov
 */
class NPlusOneEntityLoadListener implements LoadEventListener {

    private final TransactionAccessor transactionAccessor;

    NPlusOneEntityLoadListener(TransactionAccessor transactionAccessor) {
        this.transactionAccessor = transactionAccessor;
    }

    @Override
    public void onLoad(LoadEvent event, LoadType loadType) {
        if (!event.isAssociationFetch()) {
            return;
        }

        String entityClassName = event.getEntityClassName();
        Object id = event.getEntityId();

        boolean isBatch = event.getSession()
                .getFactory()
                .getMappingMetamodel()
                .getEntityDescriptor(event.getEntityClassName())
                .isBatchLoadable();

        //        transactionProfileAccessor.recordLazyCollectionLoading(new LazyLoadingTarget());

    }
}
