package com.nucleonforge.axile.sbs.spring.metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

import com.nucleonforge.axile.common.api.KeyValue;
import com.nucleonforge.axile.common.api.metrics.AxileMetricsGroups;
import com.nucleonforge.axile.common.api.metrics.AxileMetricsGroups.MetricsGroup;

/**
 * Default implementation of {@link ServiceMetricsGroupsAssembler}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultServiceMetricsGroupsAssembler implements ServiceMetricsGroupsAssembler {

    public static final String OTHER_GROUP_NAME = "Others";

    private final MeterRegistry registry;

    public DefaultServiceMetricsGroupsAssembler(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public AxileMetricsGroups assemble() {
        Map<String, List<KeyValue>> metricsByGroupName =
                listNames().stream().collect(Collectors.groupingBy(kv -> extractGroupName(kv.key())));

        List<MetricsGroup> metricsGroup = metricsByGroupName.entrySet().stream()
                .map(entry -> new MetricsGroup(entry.getKey(), entry.getValue()))
                .toList();

        return new AxileMetricsGroups(metricsGroup);
    }

    private List<KeyValue> listNames() {
        Map<String, String> metricsNameMapping = new HashMap<>();
        collectMetricNamesAndDescription(metricsNameMapping, this.registry);

        return metricsNameMapping.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new KeyValue(entry.getKey(), entry.getValue()))
                .toList();
    }

    private void collectMetricNamesAndDescription(Map<String, String> names, MeterRegistry registry) {
        if (registry instanceof CompositeMeterRegistry composite) {
            composite.getRegistries().forEach(member -> collectMetricNamesAndDescription(names, member));
        } else {
            registry.getMeters()
                    .forEach(meter -> names.putIfAbsent(
                            meter.getId().getName(), meter.getId().getDescription()));
        }
    }

    private static String extractGroupName(String it) {
        String[] parts = it.split("\\.");

        if (parts.length == 1) {
            return OTHER_GROUP_NAME;
        }

        return parts[0];
    }
}
