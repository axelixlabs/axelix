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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Internal, process-local observations grouped by Spring bean definition registry.
 * Weak keys allow closed contexts to be collected; decisions contain no Spring objects.
 * There is deliberately no endpoint or connection to the conditions feed.
 *
 * @author Axelix Labs
 */
public final class ProfileDecisionRecorder {

    private static final Map<Object, List<ProfileDecision>> decisions = new WeakHashMap<>();

    private ProfileDecisionRecorder() {}

    /** Records an actual evaluation. Public only so instrumented Spring code can call it. */
    public static synchronized void record(Object registry, ProfileDecision decision) {
        decisions.computeIfAbsent(registry, ignored -> new ArrayList<>()).add(decision);
    }

    /** Returns an immutable snapshot for the given registry, empty when nothing was observed. */
    public static synchronized List<ProfileDecision> snapshot(Object registry) {
        return List.copyOf(decisions.getOrDefault(registry, List.of()));
    }
}
