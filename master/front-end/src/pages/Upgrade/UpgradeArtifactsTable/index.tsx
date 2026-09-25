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
import { Table } from "antd";
import type { ColumnsType } from "antd/es/table";

import type { IUpgradeArtifact } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    artifacts: IUpgradeArtifact[];
}

export const UpgradeArtifactsTable = ({ artifacts }: IProps) => {
    const columns: ColumnsType<IUpgradeArtifact> = [
        {
            title: "ARTIFACT",
            dataIndex: "artifactId",
            key: "artifactId",
            render: (artifactId: string, { groupId }) => (
                <div className={styles.ArtifactCell}>
                    <span className={styles.ArtifactId}>{artifactId}</span>
                    <span className={styles.GroupId}>{groupId}</span>
                </div>
            ),
        },
        {
            title: "STARTER",
            dataIndex: "starterVersion",
            key: "starterVersion",
            width: 120,
            render: (version: string) => <span className={styles.VersionBadge}>{version}</span>,
        },
        {
            title: "PLUGIN",
            dataIndex: "pluginVersion",
            key: "pluginVersion",
            width: 120,
            render: (version: string) => <span className={styles.PluginVersion}>{version}</span>,
        },
        {
            title: "LAST SEEN",
            dataIndex: "lastSeen",
            key: "lastSeen",
            width: 150,
            render: (version: string) => <span className={styles.LastSeen}>{version}</span>,
        },
    ];

    return (
        <>
            <div className={styles.SectionRow}>
                <div className={styles.SectionTitle}>Capping the headroom</div>
                <p className={styles.SectionNote}>Bumping the starter in these services widens the headroom.</p>
            </div>

            <Table<IUpgradeArtifact>
                columns={columns}
                dataSource={artifacts}
                rowKey={(r) => r.artifactId}
                pagination={false}
                size="small"
                className={styles.Table}
            />

            <p className={`TextUltraSmall ${styles.FootNote}`}>
                Every service seen at least once in the last 30 days counts, online now or not
            </p>
        </>
    );
};
