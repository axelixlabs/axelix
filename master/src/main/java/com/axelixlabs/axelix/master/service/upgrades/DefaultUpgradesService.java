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
package com.axelixlabs.axelix.master.service.upgrades;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.utils.SemanticVersion;
import com.axelixlabs.axelix.master.api.external.response.upgrades.CeilingBlocker;
import com.axelixlabs.axelix.master.api.external.response.upgrades.StarterVersionUsage;
import com.axelixlabs.axelix.master.api.external.response.upgrades.UpgradesResponse;
import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository.LatestStarterVersion;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

import static com.axelixlabs.axelix.master.service.discovery.WindowCompatibilityDetectionStrategy.WINDOW_SIZE;

/**
 * Default implementation of {@link UpgradesService}.
 *
 * @author Nikita Kirillov
 */
@Service
public class DefaultUpgradesService implements UpgradesService {

    public static final int OBSERVATION_WINDOW_DAYS = 30;

    private final DatabaseHistoricalApplicationSnapshotService snapshotService;
    private final AxelixVersionDiscoverer axelixVersionDiscoverer;

    public DefaultUpgradesService(
            DatabaseHistoricalApplicationSnapshotService snapshotService,
            AxelixVersionDiscoverer axelixVersionDiscoverer) {
        this.snapshotService = snapshotService;
        this.axelixVersionDiscoverer = axelixVersionDiscoverer;
    }

    @Override
    public UpgradesResponse getUpgrades() {
        String masterVersion =
                SemanticVersion.parse(axelixVersionDiscoverer.getVersion()).majorMinor();

        LocalDate since = LocalDate.now(ZoneOffset.UTC).minusDays(OBSERVATION_WINDOW_DAYS);
        List<LatestStarterVersion> snapshots = snapshotService.getLatestStarterVersionsSince(since);

        SemanticVersion oldest = null;
        Map<String, Integer> serviceCountByVersion = new HashMap<>();
        int servicesTotal = 0;

        for (LatestStarterVersion snapshot : snapshots) {
            SemanticVersion version = SemanticVersion.parse(snapshot.starterVersion());

            servicesTotal++;
            serviceCountByVersion.merge(version.majorMinor(), 1, Integer::sum);

            if (oldest == null || version.isOlderThan(oldest)) {
                oldest = version;
            }
        }

        if (oldest == null) {
            return new UpgradesResponse(masterVersion, 0, WINDOW_SIZE, List.of(), List.of());
        }

        String oldestLabel = oldest.majorMinor();

        return new UpgradesResponse(
                masterVersion,
                servicesTotal,
                WINDOW_SIZE,
                buildStarterVersions(serviceCountByVersion),
                buildCeilingBlockers(snapshots, oldestLabel));
    }

    private static List<StarterVersionUsage> buildStarterVersions(Map<String, Integer> serviceCountByVersion) {
        List<StarterVersionUsage> starterVersions = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : serviceCountByVersion.entrySet()) {
            starterVersions.add(new StarterVersionUsage(entry.getKey(), entry.getValue()));
        }

        starterVersions.sort(DefaultUpgradesService::compareVersionsDescending);
        return starterVersions;
    }

    private static List<CeilingBlocker> buildCeilingBlockers(List<LatestStarterVersion> snapshots, String oldestLabel) {
        List<CeilingBlocker> ceilingBlockers = new ArrayList<>();
        for (LatestStarterVersion snapshot : snapshots) {
            Optional<SemanticVersion> parsed = SemanticVersion.tryParse(snapshot.starterVersion());
            if (parsed.isPresent() && parsed.get().majorMinor().equals(oldestLabel)) {
                ceilingBlockers.add(new CeilingBlocker(
                        snapshot.artifactId(), snapshot.groupId(), snapshot.starterVersion(), snapshot.date()));
            }
        }

        ceilingBlockers.sort(Comparator.comparing(CeilingBlocker::lastSeen));
        return ceilingBlockers;
    }

    /**
     * Orders {@code major.minor} labels newest first.
     * A plain string comparison would sort {@code 1.10} before {@code 1.9}.
     */
    private static int compareVersionsDescending(StarterVersionUsage a, StarterVersionUsage b) {
        String[] partsA = a.version().split("\\.", 2);
        String[] partsB = b.version().split("\\.", 2);

        int majorCompare = Integer.compare(Integer.parseInt(partsB[0]), Integer.parseInt(partsA[0]));
        if (majorCompare != 0) {
            return majorCompare;
        }

        return Integer.compare(Integer.parseInt(partsB[1]), Integer.parseInt(partsA[1]));
    }
}
