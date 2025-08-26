package com.nucleonforge.axile.spring.build;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.nucleonforge.axile.common.domain.JvmNonStandardOption;
import com.nucleonforge.axile.common.domain.JvmNonStandardOptions;

/**
 * Default implementation of {@link NonStandardVMOptionsDiscoverer}.
 *
 * @since 25.08.2025
 * @author Nikita Kirillov
 */
public class DefaultNonStandardVMOptionsDiscoverer implements NonStandardVMOptionsDiscoverer {

    @Override
    public JvmNonStandardOptions discover() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> inputArguments = runtimeMXBean.getInputArguments();

        Set<JvmNonStandardOption> nonStandardOptions = inputArguments.stream()
                .filter(arg -> arg.startsWith("-X") || arg.startsWith("-XX"))
                .map(JvmNonStandardOption::new)
                .collect(Collectors.toSet());

        return new JvmNonStandardOptions(nonStandardOptions);
    }
}
