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

import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test helper for demonstrating transaction propagation scenarios and monitoring.
 * Used in integration tests to verify transaction tracking behavior.
 *
 * @author Nikita Kirillov
 */
// @Component
public class PropagationTestHelper {

    private final OwnerRepository ownerRepository;
    private final PropagationTestHelper self;

    public PropagationTestHelper(OwnerRepository ownerRepository, @Lazy PropagationTestHelper self) {
        this.ownerRepository = ownerRepository;
        this.self = self;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void testRequiresNew(String lastName) {
        ownerRepository.findByLastName(lastName);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void testNestedRequiresNew() {
        ownerRepository.findByLastName("Franklin");
    }

    @Transactional(propagation = Propagation.NESTED)
    public void testNested() {
        ownerRepository.findByLastName("Schroeder");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void testMandatory(String lastName) {
        ownerRepository.findByLastName(lastName);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testNotSupported(String lastName) {
        ownerRepository.findByLastName(lastName);
    }

    @Transactional
    public void testSelfInvocation() {
        internalMethod();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void internalMethod() {
        ownerRepository.findByLastName("Black");
    }

    @Transactional
    public void testCorrectSelfInvocation() {
        self.requiresNewViaProxy();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewViaProxy() {
        ownerRepository.findByLastName("White");
    }
}
