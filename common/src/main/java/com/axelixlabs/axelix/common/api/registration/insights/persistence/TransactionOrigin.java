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
package com.axelixlabs.axelix.common.api.registration.insights.persistence;

/**
 * The origin of the transaction, i.e. how exactly the transaction was created.
 *
 * @author Mikhail Polivakha
 */
public enum TransactionOrigin {

    /**
     * Various spring infrastructure, such as TransactionalRepositoryFactoryBeanSupport can open the transaction.
     */
    SPRING_INFRASTRUCTURE,

    /**
     * Declaratively by the application code i.e. via @Transactonal
     */
    APPLICATION_DECLARATIVE,

    /**
     * Imperatively by the application code i.e. via TransactionTemplate or via TransactionManager
     */
    APPLICATION_IMPERATIVE
}
