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

import { Copy, HintTooltip } from "@/components";
import type { IEnvProperty } from "@/models";

import { EnvironmentPropertyValue } from "../../EnvironmentPropertyValue";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single property
     */
    property: IEnvProperty;

    /**
     * Reserves the caret gutter on rows that cannot be expanded, so that every row in a source lines
     * up on the same columns
     */
    caretPlaceholder?: boolean;
}

export const EnvironmentProperty = ({ property, caretPlaceholder }: IProps) => {
    const { name, deprecation, isPrimary } = property;

    const { t } = useTranslation();

    const rowStyles = [
        styles.MainWrapper,
        deprecation ? styles.FlaggedRow : "",
        !deprecation && !isPrimary ? styles.SuppressedRow : "",
        caretPlaceholder ? styles.CaretPlaceholder : "",
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <div className={rowStyles}>
            {/* Holds the caret gutter open; the caret itself is drawn by the surrounding Accordion. */}
            <span className={styles.CaretCell} />

            <div className={styles.KeyChunk}>
                <span className={styles.Key}>{name}</span>
                <Copy text={name} />
                {deprecation && (
                    <HintTooltip
                        placement="bottomLeft"
                        content={
                            <>
                                <span className={styles.TooltipLabel}>
                                    <span className={styles.TooltipDot} />
                                    {t("Environments.deprecated")}
                                </span>
                                <span>{deprecation.message}</span>
                            </>
                        }
                    >
                        <span className={styles.DeprecationChip}>
                            <span className={styles.ChipDot} />
                            {t("Environments.deprecated")}
                        </span>
                    </HintTooltip>
                )}
            </div>

            <EnvironmentPropertyValue property={property} />
        </div>
    );
};
