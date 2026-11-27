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
package com.axelixlabs.axelix.sbs.spring.core.cache;

import java.util.Map;

/**
 * Matches and removes string keys in a live local cache. Both operations observe
 * the cache as it is being iterated, so a preview can differ from a later eviction.
 */
public final class CacheEntryOperations {

    private CacheEntryOperations() {}

    public static long countMatches(Map<?, ?> entries, String pattern) {
        validatePattern(pattern);
        return entries.keySet().stream().filter(key -> matches(key, pattern)).count();
    }

    public static void evict(Map<?, ?> entries, String selector, boolean wildcard) {
        if (wildcard) {
            validatePattern(selector);
            for (Object key : entries.keySet()) {
                if (matches(key, selector)) {
                    entries.remove(key);
                }
            }
        } else {
            entries.remove(selector);
        }
    }

    private static void validatePattern(String pattern) {
        if (pattern.isBlank() || pattern.length() > 256) {
            throw new IllegalArgumentException("Wildcard pattern must contain text and be at most 256 characters");
        }
    }

    // Match the whole key. An asterisk matches any number of characters, including zero.
    // A question mark matches one character. All other characters are literal.
    private static boolean matches(Object key, String pattern) {
        if (!(key instanceof String)) {
            return false;
        }
        String value = (String) key;
        int offset = 0;
        int token = 0;
        int lastStar = -1;
        int starEnd = 0;
        while (offset < value.length()) {
            if (token < pattern.length() && pattern.charAt(token) == '*') {
                lastStar = token++;
                starEnd = offset;
            } else if (matchesCharacter(pattern, token, value.charAt(offset))) {
                offset++;
                token++;
            } else if (lastStar >= 0) {
                token = lastStar + 1;
                offset = ++starEnd;
            } else {
                return false;
            }
        }
        while (token < pattern.length() && pattern.charAt(token) == '*') {
            token++;
        }
        return token == pattern.length();
    }

    private static boolean matchesCharacter(String pattern, int token, char character) {
        return token < pattern.length() && (pattern.charAt(token) == '?' || pattern.charAt(token) == character);
    }
}
