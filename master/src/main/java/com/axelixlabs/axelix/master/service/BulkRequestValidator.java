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
package com.axelixlabs.axelix.master.service;

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Shared validation for requests targeting several instances.
 *
 * @author Sergey Cherkasov
 */
public final class BulkRequestValidator {

    private BulkRequestValidator() {}

    /**
     * Checks whether a bulk request lacks an instance ID containing text or any required text value.
     *
     * @param instanceIds the target instance IDs; must contain at least one non-blank ID
     * @param requiredFields the text values that must contain at least one non-whitespace character
     * @return {@code true} if the instance list is null, empty or contains no non-blank ID, or any required text value is
     *         null, empty or whitespace-only; {@code false} otherwise
     */
    public static boolean hasMissingRequiredFields(
            @Nullable List<String> instanceIds, @Nullable String... requiredFields) {

        if (CollectionUtils.isEmpty(instanceIds) || instanceIds.stream().noneMatch(StringUtils::hasText)) {
            return true;
        }

        for (@Nullable String field : requiredFields) {
            if (!StringUtils.hasText(field)) {
                return true;
            }
        }

        return false;
    }
}
