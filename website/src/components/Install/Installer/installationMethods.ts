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
import { EInstallMethod, TInstallMethodData } from "@/models";

export const INSTALLATION_METHODS: TInstallMethodData = {
    [EInstallMethod.K8S]: {
        description:
            "The Helm chart installs the master into your cluster. Apps discover it through cluster DNS — no extra wiring needed.",
        href: "https://axelix.io/docs/start/configuring-master#run-on-kubernetes",
    },
    [EInstallMethod.COMPOSE]: {
        description:
            "Compose defines the master as a service in your stack. Bring it up once, then point your apps at it through the Compose network.",
        href: "https://axelix.io/docs/start/configuring-master#run-with-docker-compose",
    },
    [EInstallMethod.DOCKER]: {
        description:
            "The docker installation involves pulling an image, running it, and then launching your Spring Boot microservices with the configured Axelix starter.",
        href: "https://axelix.io/docs/start/configuring-master#run-with-docker",
    },
    [EInstallMethod.BARE]: {
        description:
            "Installing Axelix on bare metal or VMs without containerization is also possible by directly launching a JAR file",
        href: "https://axelix.io/docs/start/configuring-master#run-as-a-jar",
    },
};
