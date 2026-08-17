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

import { Accordion } from "@/components";
import { groupEntityProblems } from "@/helpers";
import type { IMappedEntity } from "@/models";

import { AssociationProblemChip } from "../../AssociationProblemChip";

import { EntityDetail } from "./EntityDetail";
import styles from "./styles.module.css";

interface IProps {
    /**
     * The entity to render.
     */
    entity: IMappedEntity;
}

const MAX_CHIPS = 2;

export const EntityRow = ({ entity }: IProps) => {
    const { t } = useTranslation();

    const flaggedCount = entity.flaggedAssociations.length;
    const groupedChips = groupEntityProblems(entity);

    const shownChips = groupedChips.slice(0, MAX_CHIPS);
    const hiddenChips = groupedChips.length - shownChips.length;

    return (
        <Accordion
            wrapperStyles={styles.MainWrapper}
            headerStyles={styles.Row}
            contentStyles={styles.Content}
            header={
                <>
                    <div className={styles.Entity}>
                        <span className={`TextSmall ${styles.Name}`}>{entity.name}</span>
                        <span className={`TextUltraSmall ${styles.Subtitle}`}>@Entity · {entity.table}</span>
                    </div>
                    <div className={styles.Associations}>
                        <span className={`TextSmall ${styles.Mapped}`}>
                            {entity.associationsCount === null
                                ? "—"
                                : t("EntitiesMap.mapped", { count: entity.associationsCount })}
                        </span>
                        <span className={`TextUltraSmall ${styles.Flagged}`}>
                            {t("EntitiesMap.flagged", { count: flaggedCount })}
                        </span>
                    </div>
                    <div className={styles.Problems}>
                        {shownChips.map((group) => (
                            <AssociationProblemChip key={group.type} type={group.type} multiplicity={group.count} />
                        ))}
                        {hiddenChips > 0 && <span className={`TextUltraSmall ${styles.More}`}>+{hiddenChips}</span>}
                    </div>
                </>
            }
        >
            <EntityDetail entity={entity} />
        </Accordion>
    );
};
