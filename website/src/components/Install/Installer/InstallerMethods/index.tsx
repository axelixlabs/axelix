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
import { EInstallMethod, IInstallMethods } from "@/models";

import { INSTALLATION_METHODS } from "../installationMethods";

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
        label: "Bare Metal / VMs",
        icon: <ServerIcon width="22" height="22" />,
    },
];

interface IProps {
    installMethod: EInstallMethod;
    setInstallMethod: (method: EInstallMethod) => void;
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
                <p className={styles.Description}>{INSTALLATION_METHODS[installMethod].description}</p>
                <a
                    className={styles.Documentations}
                    href={INSTALLATION_METHODS[installMethod].href}
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Read Documentation <span className={styles.Arrow}>→</span>
                </a>
            </div>
        </aside>
    );
};
