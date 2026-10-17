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

import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.sbs.spring.core.config.HeartBeatConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.contract.heartbeatv2.HeartBeatMetadataV2;
import com.axelixlabs.axelix.sbs.spring.core.contract.initialregistrationrequest.InitialSelfRegistrationRequest;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.MemoryDetails;
import com.axelixlabs.axelix.sbs.spring.core.log.Logger;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.sbom.ClasspathDependencySbom;

/**
 * Default implementation of {@link HeartBeatMetadataAssembler}.
 *
 * @since 05.02.2026
 * @author Nikita Kirillov
 * @author Ilya Naumov
 */
public class DefaultHeartBeatMetadataAssembler implements HeartBeatMetadataAssembler {

    private static final char[] NAME_POSTFIX_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int NAME_POSTFIX_LENGTH = 8;
    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private final Random random = new Random();

    private final HeartBeatConfigurationProperties heartBeatConfigurationProperties;

    private final BasicRegistrationMetadataAssembler basicRegistrationMetadataAssembler;

    private final HealthDetectionFunction healthDetectionFunction;

    private final InsightsInfoProvider insightsInfoProvider;

    private final String instanceId;

    private final String instanceName;

    private final String deploymentAt;

    private final @Nullable String sbom;

    public DefaultHeartBeatMetadataAssembler(
            BasicRegistrationMetadataAssembler basicRegistrationMetadataAssembler,
            HealthDetectionFunction healthDetectionFunction,
            InsightsInfoProvider insightsInfoProvider,
            HeartBeatConfigurationProperties heartBeatConfigurationProperties,
            ClasspathDependencySbom classpathDependencySbom,
            Logger logger) {
        this.heartBeatConfigurationProperties = heartBeatConfigurationProperties;
        this.basicRegistrationMetadataAssembler = basicRegistrationMetadataAssembler;
        this.healthDetectionFunction = healthDetectionFunction;
        this.insightsInfoProvider = insightsInfoProvider;
        this.instanceId = UUID.randomUUID().toString();
        this.instanceName = heartBeatConfigurationProperties.getInstanceName() + "-" + generateNamePostfix();
        this.deploymentAt = Instant.now().toString();
        this.sbom = readSbom(classpathDependencySbom, logger);
    }

    @Override
    public InitialSelfRegistrationRequest assembleRegistrationRequest() {

        return new InitialSelfRegistrationRequest()
                .basicRegistrationMetadata(basicRegistrationMetadataAssembler.assemble())
                .instanceId(instanceId)
                .instanceName(instanceName)
                .instanceActuatorUrl(heartBeatConfigurationProperties.getInstanceActuatorUrl())
                .deploymentAt(deploymentAt)
                .sbom(sbom);
    }

    @Override
    public HeartBeatMetadataV2 assembleHeartBeatV2() {
        return new HeartBeatMetadataV2()
                .instanceId(instanceId)
                .healthStatus(healthDetectionFunction.get())
                .memoryDetails(new MemoryDetails()
                        .heap(memoryMXBean.getHeapMemoryUsage().getUsed()))
                .transactions(insightsInfoProvider.getTransactions())
                .scheduledTaskExecutions(insightsInfoProvider.getScheduledTaskExecutions());
    }

    private static @Nullable String readSbom(ClasspathDependencySbom classpathDependencySbom, Logger logger) {
        try {
            return classpathDependencySbom
                    .read()
                    .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                    .orElse(null);
        } catch (UncheckedIOException e) {
            logger.info("Failed to read the dependency SBOM, registering without it: {}", e.getMessage());
            return null;
        }
    }

    private String generateNamePostfix() {
        StringBuilder sb = new StringBuilder(NAME_POSTFIX_LENGTH);
        for (int i = 0; i < NAME_POSTFIX_LENGTH; i++) {
            sb.append(NAME_POSTFIX_CHARS[random.nextInt(NAME_POSTFIX_CHARS.length)]);
        }
        return sb.toString();
    }
}
