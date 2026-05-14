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
package com.axelixlabs.axelix.sbs.spring.core.transactions;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class PropagationTestHelper {

    private final OwnerRepository ownerRepository;

    public PropagationTestHelper(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRequiresNew(String lastName) {
        ownerRepository.save(new Owner().setLastName(lastName));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMultipleOwners() {
        ownerRepository.saveAll(List.of(new Owner(), new Owner(), new Owner()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOwner(Owner owner) {
        // will cause entityManager.merge --> new SELECT, since Owner has an id
        ownerRepository.save(owner);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void findOwnerById(Long id) {
        Owner owner = ownerRepository.findById(id).orElseThrow();
        owner.getPets().size(); // will cause n + 1
    }

    @Transactional(propagation = Propagation.NESTED)
    public void testNested() {
        ownerRepository.findByLastName("Schroeder");
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testNotSupported(String lastName) {
        ownerRepository.findByLastName(lastName);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void testSupports(String lastName) {
        ownerRepository.findByLastName(lastName);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void testSupportsWithoutTransaction() {}

    @Transactional
    public void testRollbackScenario(String lastName) {
        ownerRepository.findByLastName(lastName);
        throw new RuntimeException("Test rollback");
    }
}
