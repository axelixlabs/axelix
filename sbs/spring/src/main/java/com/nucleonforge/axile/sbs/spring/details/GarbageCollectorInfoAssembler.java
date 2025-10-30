package com.nucleonforge.axile.sbs.spring.details;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for retrieving garbage collector information.
 *
 * @since 30.10.2025
 * @author Nikita Kirillov
 */
public class GarbageCollectorInfoAssembler {

    private GarbageCollectorInfoAssembler(){}

    public static String getGarbageCollectorInfo() {
        try {
            List<String> gcNames = ManagementFactory.getGarbageCollectorMXBeans().stream()
                .map(GarbageCollectorMXBean::getName)
                .collect(Collectors.toList());

            if (!gcNames.isEmpty()) {
                String joined = String.join(", ", gcNames).toLowerCase();

                if (joined.contains("g1")) return "G1 GC";
                if (joined.contains("shenandoah")) return "Shenandoah GC";
                if (joined.contains("zgc")) return "ZGC";
                if (joined.contains("epsilongc") || joined.contains("epsilon")) return "Epsilon (no-op) GC";
                if (joined.contains("parallel") || joined.contains("ps marksweep") || joined.contains("ps scavenge"))
                    return "Parallel GC";
                if (joined.contains("concurrent") || joined.contains("parnew")) return "Concurrent Mark Sweep (CMS)";
                if (joined.contains("mark sweep compact") || joined.contains("copy")) return "Serial GC";

                return String.join(", ", gcNames);
            }
        } catch (Exception ignored) {
        }
        return "Unknown GC";
    }
}
