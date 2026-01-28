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
package com.axelixlabs.axelix.sbs.spring.threaddump;

/**
 * Interface to enable or disable thread contention monitoring.
 *
 * @apiNote <a href="https://github.com/openjdk/jdk/blob/master/src/java.management/share/classes/java/lang/management/ThreadMXBean.java#L364"></a>
 * Thread contention monitoring is disabled by default.
 *
 * @author Sergey Cherkasov
 */
public interface ThreadDumpContentionMonitoringManagement {

    /**
     * Enables thread contention monitoring.
     */
    void enable();

    /**
     * Disables thread contention monitoring.
     */
    void disable();
}
