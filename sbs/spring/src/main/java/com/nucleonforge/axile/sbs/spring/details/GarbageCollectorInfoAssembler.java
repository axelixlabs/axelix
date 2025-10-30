package com.nucleonforge.axile.sbs.spring.details;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for retrieving garbage collector information.
 *
 * @since 30.10.2025
 * @author Nikita Kirillov
 */
public class GarbageCollectorInfoAssembler {

    private static final Logger log = LoggerFactory.getLogger(GarbageCollectorInfoAssembler.class);

    private GarbageCollectorInfoAssembler() {}

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public static String getGarbageCollectorInfo() {
        try {
            List<String> gcNames = ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .map(GarbageCollectorMXBean::getName)
                    .collect(Collectors.toList());

            if (!gcNames.isEmpty()) {
                String joined = String.join(", ", gcNames).toLowerCase();

                GarageCollector garageCollector = GarageCollector.fromName(joined);

                if (garageCollector == GarageCollector.UNKNOWN) {
                    return String.join(", ", gcNames);
                }

                return garageCollector.name();
            }
        } catch (Exception exception) {
            log.warn(
                    "Unable to determine the GC used inside the given application. Falling back to UNKNOWN", exception);
        }

        return GarageCollector.UNKNOWN.name();
    }
}
