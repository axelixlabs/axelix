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
import type { TFunction } from "i18next";
import type { JSX } from "react";
import { useTranslation } from "react-i18next";

import type { ICeilingBlocker } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    ceilingBlockers: ICeilingBlocker[];
}

// TODO: Improve this component in the future

const renderFootNoteSummary = (t: TFunction) => (): JSX.Element => (
    <Table.Summary.Row>
        <Table.Summary.Cell index={0} colSpan={3}>
            <p className={`TextUltraSmall ${styles.FootNote}`}>{t("Upgrades.ArtifactsTable.footNote")}</p>
        </Table.Summary.Cell>
    </Table.Summary.Row>
);

export const UpgradesTable = ({ ceilingBlockers }: IProps) => {
    const { t } = useTranslation();

    const columns: ColumnsType<ICeilingBlocker> = [
        {
            title: t("Upgrades.ArtifactsTable.artifact"),
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
            title: t("Upgrades.ArtifactsTable.starter"),
            dataIndex: "starterVersion",
            key: "starterVersion",
            width: 120,
            render: (version: string) => <span className={styles.VersionBadge}>{version}</span>,
        },
        {
            title: t("Upgrades.ArtifactsTable.lastSeen"),
            dataIndex: "lastSeen",
            key: "lastSeen",
            width: 150,
            render: (lastSeen: string) => <span className={styles.LastSeen}>{lastSeen}</span>,
        },
    ];

    return (
        <>
            <div className={styles.SectionRow}>
                <div className={styles.SectionTitle}>{t("Upgrades.ArtifactsTable.sectionTitle")}</div>
                <p className={styles.SectionNote}>{t("Upgrades.ArtifactsTable.sectionNote")}</p>
            </div>

            <Table<ICeilingBlocker>
                columns={columns}
                dataSource={ceilingBlockers}
                rowKey={(r) => r.artifactId}
                pagination={false}
                size="small"
                summary={renderFootNoteSummary(t)}
                className={styles.Table}
            />
        </>
    );
};
