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
package com.axelixlabs.axelix.master.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Persistent role record, holding the columns of the {@code roles} table itself.
 *
 * @param id          Unique identifier of the role. Serves as the primary key.
 * @param name        Unique name of the role (e.g. {@code ADMIN}, {@code EDITOR}, {@code VIEWER}).
 * @param description Human-readable description of what the role allows.
 * @param roleOrigin  Origin of the role.
 *
 * @author Sergey Cherkasov
 */
@Table("roles")
public record RoleEntity(@Id String id, String name, String description, RoleOrigin roleOrigin) {}
