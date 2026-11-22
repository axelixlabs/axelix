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
import com.axelixlabs.axelix.master.api.external.response.upgrades.ObservedService;
import com.axelixlabs.axelix.master.api.external.response.upgrades.StarterVersionUsage;
import com.axelixlabs.axelix.master.api.external.response.upgrades.UpgradeImpactResponse;
import com.axelixlabs.axelix.master.api.external.response.upgrades.UpgradesResponse;
import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository.LatestStarterVersion;
import com.axelixlabs.axelix.master.service.discovery.WindowCompatibilityDetectionStrategy;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;
import com.axelixlabs.axelix.master.utils.SemanticVersion;

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

        List<ObservedService> services = new ArrayList<>();
        Map<String, Integer> serviceCountByVersion = new HashMap<>();

        for (LatestStarterVersion snapshot : snapshots) {
            Optional<SemanticVersion> parsed = SemanticVersion.tryParse(snapshot.starterVersion());
            if (parsed.isEmpty()) {
                continue;
            }
            SemanticVersion version = parsed.get();

            serviceCountByVersion.merge(version.majorMinor(), 1, Integer::sum);

            services.add(new ObservedService(
                    snapshot.artifactId(), snapshot.groupId(), snapshot.starterVersion(), snapshot.date()));
        }

        services.sort(Comparator.comparing((ObservedService service) -> SemanticVersion.parse(service.starterVersion()))
                .thenComparing(ObservedService::lastSeen)
                .thenComparing(ObservedService::groupId)
                .thenComparing(ObservedService::artifactId));

        return new UpgradesResponse(masterVersion, WINDOW_SIZE, buildStarterVersions(serviceCountByVersion), services);
    }

    @Override
    public UpgradeImpactResponse getUpgradeImpact(String targetVersion) {
        SemanticVersion target = SemanticVersion.tryParseMajorMinor(targetVersion)
                .orElseThrow(() -> new BadRequestException("Not a valid major.minor version: " + targetVersion));

        LocalDate since = LocalDate.now(ZoneOffset.UTC).minusDays(OBSERVATION_WINDOW_DAYS);

        int lostApplications = 0;
        for (LatestStarterVersion snapshot : snapshotService.getLatestStarterVersionsSince(since)) {
            if (!WindowCompatibilityDetectionStrategy.isCompatible(
                    target, SemanticVersion.parse(snapshot.starterVersion()))) {
                lostApplications++;
            }
        }

        return new UpgradeImpactResponse(targetVersion, lostApplications);
    }

    private static List<StarterVersionUsage> buildStarterVersions(Map<String, Integer> serviceCountByVersion) {
        List<StarterVersionUsage> starterVersions = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : serviceCountByVersion.entrySet()) {
            starterVersions.add(new StarterVersionUsage(entry.getKey(), entry.getValue()));
        }

        starterVersions.sort(DefaultUpgradesService::compareVersionsDescending);
        return starterVersions;
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
