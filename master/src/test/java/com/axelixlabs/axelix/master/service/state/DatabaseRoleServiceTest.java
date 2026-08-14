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
package com.axelixlabs.axelix.master.service.state;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.utils.TestRoles;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests of {@link DatabaseRoleService}.
 *
 * @author Sergey Cherkasov
 */
@DatabaseMatrixTest
class DatabaseRoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Test
    void findByName_shouldReturnBuiltInRolesMatchingTheOnesTestsAreBuiltUpon() {
        // when.
        Optional<Role> viewer = roleService.findByName(TestRoles.VIEWER.getName());
        Optional<Role> editor = roleService.findByName(TestRoles.EDITOR.getName());
        Optional<Role> admin = roleService.findByName(TestRoles.ADMIN.getName());

        // then.
        assertThat(viewer).hasValueSatisfying(role -> assertGrantsSameAs(role, TestRoles.VIEWER));
        assertThat(editor).hasValueSatisfying(role -> assertGrantsSameAs(role, TestRoles.EDITOR));
        assertThat(admin).hasValueSatisfying(role -> assertGrantsSameAs(role, TestRoles.ADMIN));
    }

    @Test
    void findByName_shouldNotResolveInternalRoles() {
        // when.
        Optional<Role> superAdmin = roleService.findByName("SUPER_ADMIN");
        Optional<Role> managedService = roleService.findByName("MANAGED_SERVICE");

        // then.
        assertThat(superAdmin).isEmpty();
        assertThat(managedService).isEmpty();
    }

    @Test
    void findByName_shouldReturnEmptyForAnUnknownRole() {
        // when.
        Optional<Role> role = roleService.findByName("THERE_IS_NO_SUCH_ROLE");

        // then.
        assertThat(role).isEmpty();
    }

    private static void assertGrantsSameAs(Role actual, Role expected) {
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getAuthorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrderElementsOf(expected.getAuthorities().stream()
                        .map(Authority::getName)
                        .toList());
    }
}
