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
package com.axelixlabs.axelix.sbs.spring.core.conditions.profile;

import java.util.List;

/**
 * One observed Spring profile evaluation, not a claim about the final bean registry.
 * An empty method name denotes a class declaration. Repeated evaluations are retained.
 *
 * @author Axelix Labs
 */
public record ProfileDecision(
        String className,
        String methodName,
        List<String> profileExpressions,
        List<String> activeProfiles,
        List<String> defaultProfiles,
        boolean matched) {

    public ProfileDecision {
        profileExpressions = List.copyOf(profileExpressions);
        activeProfiles = List.copyOf(activeProfiles);
        defaultProfiles = List.copyOf(defaultProfiles);
    }
}
