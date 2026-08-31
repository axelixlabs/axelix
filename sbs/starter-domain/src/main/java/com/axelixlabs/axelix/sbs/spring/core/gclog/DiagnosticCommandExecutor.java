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
package com.axelixlabs.axelix.sbs.spring.core.gclog;

import java.lang.management.ManagementFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.sun.management.DiagnosticCommandMBean;

/**
 * Invokes HotSpot diagnostic commands - via the {@link DiagnosticCommandMBean}.
 *
 * @since 29.12.2025
 * @author Nikita Kirillov
 */
public class DiagnosticCommandExecutor {

    private static final ObjectName DIAGNOSTIC_COMMAND_MBEAN = diagnosticCommandMBeanName();
    private static final MBeanServer PLATFORM_MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();

    /**
     * Invokes the given {@link DiagnosticCommandMBean} operation (e.g. {@code "vmLog"}) with the given
     * arguments (e.g. {@code "list"}, {@code "what=gc=info"}) and returns its textual output.
     *
     * @throws JMException if the operation is unknown, its arguments are invalid, or the invocation fails
     */
    public String execute(String operation, String... args) throws JMException {
        return (String) PLATFORM_MBEAN_SERVER.invoke(
                DIAGNOSTIC_COMMAND_MBEAN, operation, new Object[] {args}, new String[] {String[].class.getName()});
    }

    private static ObjectName diagnosticCommandMBeanName() {
        try {
            return new ObjectName("com.sun.management:type=DiagnosticCommand");
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException(e);
        }
    }
}
