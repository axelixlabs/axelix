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
package com.axelixlabs.axelix.master.service.discovery;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.repository.InstanceRepository;

/**
 * Evicts self-registered instances that haven't sent a heartbeat within the timeout period.
 *
 * @author Nikita Kirillov
 */
@Service
public class SelfRegistrationInstanceEvictionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SelfRegistrationInstanceEvictionScheduler.class);

    private final InstanceRepository instanceRepository;

    /**
     * Maximum time in seconds without a heartbeat before a self-registered instance
     * is considered stale and eligible for eviction. Default: 45 seconds
     */
    @Value("${axelix.master.discovery.self.eviction.heartbeat-timeout:45000}")
    private long heartbeatTimeout;

    public SelfRegistrationInstanceEvictionScheduler(InstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    @Scheduled(
            fixedDelayString = "${axelix.master.discovery.self.eviction.interval:60000}",
            initialDelayString = "${axelix.master.discovery.self.eviction.interval:60000}")
    public void evictStaleInstances() {
        Instant threshold = Instant.now().minusMillis(heartbeatTimeout);
        int evictedCount = instanceRepository.deleteWhereHeartbeatOlderThan(threshold);

        if (logger.isDebugEnabled()) {
            logger.debug("Evicted {} stale instances.", evictedCount);
        }
    }
}
