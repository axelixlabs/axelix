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
package com.axelixlabs.axelix.sbs.spring.transactions;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test service for demonstrating transaction propagation scenarios and monitoring.
 * Used in integration tests to verify transaction tracking behavior.
 *
 * @author Nikita Kirillov
 */
// @Component
public class PropagationTestService {

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

    public void testFromNonTransactional(String lastName) {
        helperService.testRequiresNew(lastName);
    }

    @Transactional
    protected void testRollbackScenario(String lastName) {
        ownerRepository.findByLastName(lastName);
        helperService.testNestedRequiresNew();
        throw new RuntimeException("Test rollback");
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void testSupports(String lastName) {
        ownerRepository.findByLastName(lastName);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void testSupportsWithoutTransaction() {}
}
