/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.details;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;

// TODO: Add Azul GC
// TODO: Are we sure that the aliases provided here are even correct? Double check
/**
 * Gar
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public enum GarageCollector {

    /**
     * G1 GC
     */
    G1("g1"),

    /**
     * Shenandoah GC (Non-generational)
     */
    SHENADOAH("shenandoah"),

    /**
     * Generational shenandoah GC
     */
    GEN_SHENADOAH("generational shenandoah"),

    /**
     * Oracle ZGC GC
     */
    ZGC("zgc"),

    /**
     * Epsilon GC
     */
    EPSILONGC("epsilongc", "epsilon"),

    /**
     * Parallel collector
     */
    PARALLEL("parallel", "ps marksweep", "ps scavenge"),

    /**
     * Serial collector
     */
    SERIAL("marksweepcompact", "copy"),

    /**
     * Concurrent Mark and Sweep collector.
     */
    CMS("concurrent", "parnew"),

    /**
     * Unknown GC.
     */
    UNKNOWN();

    GarageCollector(String... aliases) {
        this.aliases = Arrays.stream(aliases).collect(Collectors.toSet());
    }

    private final Set<String> aliases;

    public static GarageCollector fromName(String name) {
        String lowerCase = name.toLowerCase();

        for (GarageCollector value : values()) {
            for (String alias : value.aliases) {
                if (lowerCase.contains(alias)) {
                    return value;
                }
            }
        }

        return GarageCollector.UNKNOWN;
    }
}
