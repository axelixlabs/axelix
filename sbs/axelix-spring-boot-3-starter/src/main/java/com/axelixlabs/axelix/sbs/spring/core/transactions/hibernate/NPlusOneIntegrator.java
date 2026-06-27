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

import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * Hibernate integrator responsible for registering N+1 tracking event listeners.
 *
 * @author Nikita Kirillov
 */
public class NPlusOneIntegrator implements Integrator {

    private final NPlusOneEntityLoadListener loadListener;
    private final NPlusOneCollectionLoadListener collectionListener;

    public NPlusOneIntegrator(
            NPlusOneEntityLoadListener loadListener, NPlusOneCollectionLoadListener collectionListener) {
        this.loadListener = loadListener;
        this.collectionListener = collectionListener;
    }

    @Override
    public void integrate(
            Metadata metadata, BootstrapContext bootstrapContext, SessionFactoryImplementor sessionFactory) {
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

        registry.prependListeners(EventType.LOAD, loadListener);
        registry.prependListeners(EventType.INIT_COLLECTION, collectionListener);
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {}
}
