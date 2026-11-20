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

import { ENTITY_CHECK_LABELS, type IEntityFinding } from "@/helpers";

import panelStyles from "../../styles.module.css";

import styles from "./styles.module.css";

interface IProps {
    finding: IEntityFinding;
    initiallyOpen: boolean;
}

export const FindingCard = ({ finding, initiallyOpen }: IProps) => {
    const { t } = useTranslation();
    const { check, location, member, annotation } = finding;
    const label = ENTITY_CHECK_LABELS.get(check);
    const heading = (
        <span>
            <strong>{label ? t(`EntitiesMap.problems.${label}`) : check}</strong>
            <span className={styles.Location}>
                {t(`EntitiesMap.locations.${location}`)} <code className={panelStyles.Code}>{member}</code>
            </span>
        </span>
    );

    if (!label) {
        return (
            <div className={styles.Finding}>
                <div className={styles.FindingHeading}>{heading}</div>
            </div>
        );
    }

    return (
        <details className={`${styles.Finding} ${panelStyles.Disclosure}`} open={initiallyOpen}>
            <summary className={`${styles.FindingHeading} ${panelStyles.DisclosureHeader}`}>{heading}</summary>
            <div className={panelStyles.DisclosureContent}>
                <div className={styles.FindingBody}>
                    {annotation && <code className={panelStyles.Code}>{annotation}</code>}
                    <p>{t(`EntitiesMap.problemFixes.${label}`)}</p>
                </div>
            </div>
        </details>
    );
};
