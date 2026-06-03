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

import { Dispatch, SetStateAction } from "react";

import { Method } from "../../../../models";

import styles from "./styles.module.css";

const METHODS: { id: Method; label: string; icon: React.ReactNode }[] = [
    {
        id: "docker",
        label: "Docker",
        icon: <DockerIcon width="22" height="22" />,
    },
    {
        id: "compose",
        label: "Docker Compose",
        icon: <ComposeIcon width="22" height="22" />,
    },
    {
        id: "k8s",
        label: "Kubernetes",
        icon: <K8sIcon width="22" height="22" />,
    },
    {
        id: "bare",
        label: "Bare Metal",
        icon: <ServerIcon width="22" height="22" />,
    },
];

const METHODS_DATA: Record<
    Method,
    {
        description: string;
        href: string;
    }
> = {
    k8s: {
        description:
            "The Helm chart installs the master into your cluster. Apps discover it through cluster DNS — no extra wiring needed.",
        href: "https://axelix.io/docs/installation/configuring-master#run-on-kubernetes",
    },
    compose: {
        description:
            "Compose defines the master as a service in your stack. Bring it up once, then point your apps at it through the Compose network.",
        href: "https://axelix.io/docs/installation/configuring-master#run-with-docker-compose",
    },
    docker: {
        description:
            "The docker installation involves pulling an image, running it, and then launching your Spring Boot microservices with the configured Axelix starter.",
        href: "https://axelix.io/docs/installation/configuring-master#run-with-docker",
    },
    bare: {
        description:
            "Installing Axelix on bare metal without containerization is also possible by directly launching a JAR file",
        href: "https://axelix.io/docs/installation/configuring-master#run-as-a-jar",
    },
};

interface IProps {
    method: Method;
    setMethod: Dispatch<SetStateAction<Method>>;
}

export const InstallerMethods = ({ method, setMethod }: IProps) => {
    return (
        <aside className={styles.MainWrapper}>
            {METHODS.map(({ id, icon, label }) => (
                <button
                    key={id}
                    className={`${styles.MethodButton} ${method === id ? styles.ActiveButton : ""}`}
                    type="button"
                    onClick={() => setMethod(id)}
                >
                    <span className={styles.Icon}>{icon}</span>
                    {label}
                </button>
            ))}

            <div className={styles.MethodFooter}>
                <p className={styles.Description}>{METHODS_DATA[method].description}</p>
                <a
                    className={styles.Documentations}
                    href={METHODS_DATA[method].href}
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Read Documentation <span className={styles.Arrow}>→</span>
                </a>
            </div>
        </aside>
    );
};
