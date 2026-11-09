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
package com.axelixlabs.axelix.master.testfixtures;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.service.state.auth.RoleService;

/**
 * Database-backed access to built-in roles for tests.
 *
 * @author Sergey Cherkasov
 */
@Component
public final class TestRoles {
    private final RoleService roleService;

    public TestRoles(RoleService roleService) {
        this.roleService = roleService;
    }

    public Role viewer() {
        return roleService.findByName("VIEWER").orElseThrow();
    }

    public Role editor() {
        return roleService.findByName("EDITOR").orElseThrow();
    }

    public Role admin() {
        return roleService.findByName("ADMIN").orElseThrow();
    }
}
