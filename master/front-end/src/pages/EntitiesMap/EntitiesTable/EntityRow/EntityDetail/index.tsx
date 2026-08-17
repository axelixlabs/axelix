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

import { HintTooltip } from "@/components";
import { orderAssociationProblems } from "@/helpers";
import type { IMappedEntity } from "@/models";
import {
    associationProblemClassToken,
    associationProblemColor,
    associationProblemFixKey,
    associationProblemLabelKey,
} from "@/utils";

import { AssociationProblemChip } from "../../../AssociationProblemChip";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The entity whose expanded detail is rendered.
     */
    entity: IMappedEntity;
}

export const EntityDetail = ({ entity }: IProps) => {
    const { t } = useTranslation();

    const flaggedCount = entity.flaggedAssociations.length;
    const mappedLabel = entity.associationsCount === null ? "—" : String(entity.associationsCount);

    return (
        <div className={styles.Detail}>
            <div className={styles.Meta}>
                <div className={styles.MetaItem}>
                    <span className={styles.MetaLabel}>{t("EntitiesMap.detail.table")}</span>
                    <span className={styles.MetaBadge}>{entity.table}</span>
                </div>
                <div className={styles.MetaDivider} />
                <div className={styles.MetaItem}>
                    <span className={styles.MetaLabel}>{t("EntitiesMap.detail.associations")}</span>
                    <span className={styles.MetaBadge}>
                        {t("EntitiesMap.detail.associationsValue", { mapped: mappedLabel, flagged: flaggedCount })}
                    </span>
                </div>
            </div>

            {entity.flaggedAssociations.map((flagged) => {
                const problems = orderAssociationProblems(flagged.problems);
                const primary = problems[0];

                return (
                    <div
                        key={`${flagged.association.entity}.${flagged.association.field}`}
                        className={`${styles.ProblemCard} ${styles[associationProblemClassToken[primary]]}`}
                    >
                        <div className={styles.ProblemHeader}>
                            <span className={styles.Field}>
                                {flagged.association.entity}.{flagged.association.field}
                            </span>
                            <div className={styles.Badges}>
                                {problems.map((problem) => (
                                    <HintTooltip
                                        key={problem}
                                        content={
                                            <>
                                                <div className={styles.HintHeader}>
                                                    <span
                                                        className={styles.HintDot}
                                                        style={{ backgroundColor: associationProblemColor[problem] }}
                                                    />
                                                    <span
                                                        className={styles.HintTitle}
                                                        style={{ color: associationProblemColor[problem] }}
                                                    >
                                                        {t(associationProblemLabelKey[problem])}
                                                    </span>
                                                </div>
                                                <div>{t(associationProblemFixKey[problem])}</div>
                                            </>
                                        }
                                    >
                                        <span className={styles.Badge}>
                                            <AssociationProblemChip type={problem} withHint />
                                        </span>
                                    </HintTooltip>
                                ))}
                            </div>
                        </div>
                        <div className={styles.Mapping}>{flagged.mapping}</div>
                    </div>
                );
            })}
        </div>
    );
};
