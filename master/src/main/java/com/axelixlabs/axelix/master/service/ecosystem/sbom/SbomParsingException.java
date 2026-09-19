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
package com.axelixlabs.axelix.master.service.ecosystem.sbom;

/**
 * Raised when the SBOM served by a managed application cannot be parsed. This points at a defect of the build plugin
 * that generated the document, or at a document Axelix has no business reading - either way not something the caller
 * can recover from.
 *
 * @author Mikhail Polivakha
 */
public class SbomParsingException extends RuntimeException {

    public SbomParsingException(String message) {
        super(message);
    }

    public SbomParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
