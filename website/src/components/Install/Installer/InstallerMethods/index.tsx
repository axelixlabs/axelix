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
import { ComposeIcon, DockerIcon, K8sIcon, ServerIcon } from "@/assets";
import { EInstallMethod, IInstallMethods, TInstallMethodData } from "@/models";

import { Dispatch, SetStateAction } from "react";

import styles from "./styles.module.css";

const METHODS: IInstallMethods[] = [
    {
        key: EInstallMethod.DOCKER,
        label: "Docker",
        icon: <DockerIcon width="22" height="22" />,
    },
    {
        key: EInstallMethod.COMPOSE,
        label: "Docker Compose",
        icon: <ComposeIcon width="22" height="22" />,
    },
    {
        key: EInstallMethod.K8S,
        label: "Kubernetes",
        icon: <K8sIcon width="22" height="22" />,
    },
    {
        key: EInstallMethod.BARE,
        label: "Bare Metal",
        icon: <ServerIcon width="22" height="22" />,
    },
];

const METHODS_DATA: TInstallMethodData = {
    [EInstallMethod.K8S]: {
        description:
            "The Helm chart installs the master into your cluster. Apps discover it through cluster DNS — no extra wiring needed.",
        href: "https://axelix.io/docs/installation/configuring-master#run-on-kubernetes",
    },
    [EInstallMethod.COMPOSE]: {
        description:
            "Compose defines the master as a service in your stack. Bring it up once, then point your apps at it through the Compose network.",
        href: "https://axelix.io/docs/installation/configuring-master#run-with-docker-compose",
    },
    [EInstallMethod.DOCKER]: {
        description:
            "The docker installation involves pulling an image, running it, and then launching your Spring Boot microservices with the configured Axelix starter.",
        href: "https://axelix.io/docs/installation/configuring-master#run-with-docker",
    },
    [EInstallMethod.BARE]: {
        description:
            "Installing Axelix on bare metal without containerization is also possible by directly launching a JAR file",
        href: "https://axelix.io/docs/installation/configuring-master#run-as-a-jar",
    },
};

interface IProps {
    installMethod: EInstallMethod;
    setInstallMethod: Dispatch<SetStateAction<EInstallMethod>>;
}

export const InstallerMethods = ({ installMethod, setInstallMethod }: IProps) => {
    return (
        <aside className={styles.MainWrapper}>
            {METHODS.map(({ key, icon, label }) => (
                <button
                    key={key}
                    className={`${styles.MethodButton} ${installMethod === key ? styles.ActiveButton : ""}`}
                    type="button"
                    onClick={() => setInstallMethod(key)}
                >
                    <span className={styles.Icon}>{icon}</span>
                    {label}
                </button>
            ))}

            <div className={styles.MethodFooter}>
                <p className={styles.Description}>{METHODS_DATA[installMethod].description}</p>
                <a
                    className={styles.Documentations}
                    href={METHODS_DATA[installMethod].href}
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Read Documentation <span className={styles.Arrow}>→</span>
                </a>
            </div>
        </aside>
    );
};
