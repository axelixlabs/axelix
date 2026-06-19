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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.HotSpot;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.Insight;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.InsightFeature;
import com.axelixlabs.axelix.sbs.spring.core.gclog.JcmdExecutor;
import com.axelixlabs.axelix.sbs.spring.core.gclog.ProcessResult;

/**
 * Default {@link InsightsInfoProvider} based on the current service runtime configuration.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public class DefaultInsightsInfoProvider implements InsightsInfoProvider {
    private final List<String> vmOptions;
    private final OpenSessionInViewStateProvider openSessionInViewStateProvider;
    private final JcmdExecutor jcmdExecutor;
    private volatile @Nullable String pid;

    /**
     * Creates a new DefaultInsightsInfoProvider.
     *
     * @param vmOptions                      list of non-standard (i.e. -X or -XX) VM options.
     * @param openSessionInViewStateProvider provider of the Spring Open Session in View state.
     * @param jcmdExecutor                   the JCMD executor used to inspect JVM logging state.
     */
    public DefaultInsightsInfoProvider(
            List<String> vmOptions,
            OpenSessionInViewStateProvider openSessionInViewStateProvider,
            JcmdExecutor jcmdExecutor) {
        this.vmOptions = vmOptions;
        this.openSessionInViewStateProvider = openSessionInViewStateProvider;
        this.jcmdExecutor = jcmdExecutor;
    }

    @Override
    public Insight getInsight() {
        String gcLogStatus = getGcLogStatus();

        return new Insight(
                new HotSpot(
                        List.of(getAppCdsFeature(), getAotCacheFeature()),
                        List.of(getGcLoggingFeature(gcLogStatus), getGcLogFileSpecifiedFeature(gcLogStatus)),
                        List.of(getCompressedObjectHeadersFeature())),
                List.of(new InsightFeature("OSIV", openSessionInViewStateProvider.isOpenSessionInViewEnabled())));
    }

    private InsightFeature getAppCdsFeature() {
        boolean enabled = false;

        for (String arg : vmOptions) {
            // Check if explicitly disabled (highest priority)
            if (arg.equals("-Xshare:off")) {
                enabled = false;
                break;
            }

            // Check for SharedArchiveFile option (indicates AppCDS usage)
            if (arg.startsWith("-XX:SharedArchiveFile=")) {
                enabled = true;
            }
        }

        return new InsightFeature("AppCDS", enabled);
    }

    private InsightFeature getAotCacheFeature() {
        boolean enabled = false;

        for (String arg : vmOptions) {
            // Check for AOTLibrary option (indicates AOT usage)
            if (arg.startsWith("-XX:AOTCache=")) {
                enabled = true;
                break;
            }
        }

        return new InsightFeature("AotCache", enabled);
    }

    private InsightFeature getCompressedObjectHeadersFeature() {
        boolean enabled = false;

        for (String arg : vmOptions) {
            // Check for explicit enable flag
            if (arg.equals("-XX:+UseCompactObjectHeaders")) {
                enabled = true;
                break;
            }
        }

        return new InsightFeature("Compressed Object Headers", enabled);
    }

    private String getGcLogStatus() {
        try {
            ProcessResult result = jcmdExecutor.execute("jcmd", getPid(), "VM.log", "list");
            if (!result.isSuccess()) {
                return "";
            }

            return result.getOutput();
        } catch (RuntimeException e) {
            return "";
        }
    }

    // TODO At present, this placeholder will remain in place until the GC Logging status tracking mechanism is improved
    // https://github.com/axelixlabs/axelix/issues/573
    private InsightFeature getGcLoggingFeature(String gcLogStatus) {
        return new InsightFeature("GC Logging", false);
    }

    private InsightFeature getGcLogFileSpecifiedFeature(String gcLogStatus) {
        for (String line : gcLogStatus.split("\n")) {
            String trim = line.trim();

            if (isGcLogLine(trim) && trim.contains("file=")) {
                return new InsightFeature("GC Log file Specified", true);
            }
        }

        return new InsightFeature("GC Log file Specified", false);
    }

    private boolean isGcLogLine(String line) {
        return line.startsWith("#") && line.contains("gc=");
    }

    private String getPid() {
        if (pid == null) {
            synchronized (this) {
                if (pid == null) {
                    pid = String.valueOf(ProcessHandle.current().pid());
                }
            }
        }
        return pid;
    }
}
