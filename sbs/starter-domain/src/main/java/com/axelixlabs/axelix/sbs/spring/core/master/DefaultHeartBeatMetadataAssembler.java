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

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import com.axelixlabs.axelix.common.api.registration.HeartBeatMetadata;
import com.axelixlabs.axelix.sbs.spring.core.config.HeartBeatConfigurationProperties;

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

    private final Random random = new Random();

    private final HeartBeatConfigurationProperties heartBeatConfigurationProperties;

    private final BasicRegistrationMetadataAssembler basicRegistrationMetadataAssembler;

    private final String instanceId;

    private final String instanceName;

    private final String deploymentAt;

    public DefaultHeartBeatMetadataAssembler(
            BasicRegistrationMetadataAssembler basicRegistrationMetadataAssembler,
            HeartBeatConfigurationProperties heartBeatConfigurationProperties) {
        this.heartBeatConfigurationProperties = heartBeatConfigurationProperties;
        this.basicRegistrationMetadataAssembler = basicRegistrationMetadataAssembler;
        this.instanceId = UUID.randomUUID().toString();
        this.instanceName = heartBeatConfigurationProperties.getInstanceName() + "-" + generateNamePostfix();
        this.deploymentAt = Instant.now().toString();
    }

    @Override
    public HeartBeatMetadata assemble() {

        return new HeartBeatMetadata(
                basicRegistrationMetadataAssembler.assemble(),
                instanceId,
                instanceName,
                heartBeatConfigurationProperties.getInstanceActuatorUrl(),
                deploymentAt);
    }

    private String generateNamePostfix() {
        StringBuilder sb = new StringBuilder(NAME_POSTFIX_LENGTH);
        for (int i = 0; i < NAME_POSTFIX_LENGTH; i++) {
            sb.append(NAME_POSTFIX_CHARS[random.nextInt(NAME_POSTFIX_CHARS.length)]);
        }
        return sb.toString();
    }
}
