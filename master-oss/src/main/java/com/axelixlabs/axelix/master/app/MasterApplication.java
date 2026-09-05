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
package com.axelixlabs.axelix.master.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClientAutoConfiguration;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The entrypoint of the OSS (community) distribution of Axelix Master.
 *
 * @author Mikhail Polivakha
 */
@SpringBootApplication(
        exclude = {CompositeDiscoveryClientAutoConfiguration.class, SimpleDiscoveryClientAutoConfiguration.class})
@ComponentScan(
        basePackages = {"com.axelixlabs.axelix.master"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = AutoConfiguration.class))
@EnableJdbcRepositories(basePackages = {"com.axelixlabs.axelix.master"})
@EnableScheduling
public class MasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MasterApplication.class, args);
    }
}
