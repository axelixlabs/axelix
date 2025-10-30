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
