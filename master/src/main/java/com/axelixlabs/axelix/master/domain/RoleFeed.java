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

/**
 * A single entry of the roles feed: one role, seen from the outside.
 *
 * @param id           Unique identifier of the role.
 * @param name         Unique name of the role.
 * @param membersCount Number of users the role is assigned to.
 * @param description  Human-readable description of what the role allows.
 *
 * @author Sergey Cherkasov
 */
public record RoleFeed(String id, String name, int membersCount, String description) {}
