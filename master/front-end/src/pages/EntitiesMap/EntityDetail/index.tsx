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
import { useTranslation } from "react-i18next";

import type { IEntityResult } from "@/helpers";

import panelStyles from "../styles.module.css";

import { FindingCard } from "./FindingCard";
import styles from "./styles.module.css";

interface IProps {
    /** The selected entity and its findings that match the active filters. */
    result: IEntityResult;
}

export const EntityDetail = ({ result: { entity, findings } }: IProps) => {
    const { t } = useTranslation();

    return (
        <section className={panelStyles.Panel} aria-labelledby="entities-map-entity">
            <header className={panelStyles.PanelHeader}>
                <h2 id="entities-map-entity" className={styles.EntityTitle}>
                    {entity.name}
                </h2>
                <p className={styles.Metadata}>
                    <code className={panelStyles.Code}>{entity.table}</code>
                    {typeof entity.associationsCount === "number" && (
                        <>
                            {" "}
                            ·{" "}
                            {t("EntitiesMap.associationCount", {
                                count: entity.associationsCount,
                            })}
                        </>
                    )}
                </p>
            </header>

            <div className={styles.Findings}>
                <h3>
                    {t("EntitiesMap.findings")} · {findings.length}
                </h3>
                {findings.length === 0 && <p>{t("EntitiesMap.noFindings")}</p>}
                {findings.map((finding, index) => (
                    <FindingCard
                        key={JSON.stringify([finding.check, finding.location, finding.member])}
                        finding={finding}
                        initiallyOpen={index === 0}
                    />
                ))}
            </div>
        </section>
    );
};
