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

import { InstanceCommit, InstanceCurrentTag, InstanceStatusTag, InstanceUptime } from "@/components/InstanceChips";
import type { TInstanceOption } from "@/models";

import contextStyles from "../../styles.module.css";

import styles from "./styles.module.css";

interface IProps {
    /** Instance data shown in this row */
    instance: TInstanceOption;

    /** Whether this row is selected */
    selected: boolean;

    /** Selects this row */
    onSelect: (instance: TInstanceOption) => void;
}

export const InstanceRow = ({ instance, selected, onSelect }: IProps) => {
    const { t } = useTranslation();

    return (
        <button
            type="button"
            role="menuitemradio"
            aria-checked={selected}
            aria-label={t("InstanceContext.selectInstance", { name: instance.name })}
            title={instance.name}
            className={`${styles.MainWrapper} ${selected ? styles.Selected : ""}`}
            onClick={() => onSelect(instance)}
        >
            <span className={styles.Details}>
                <span className={styles.Name}>{instance.name}</span>
                <span className={styles.Commit}>
                    <InstanceCommit commitShaShort={instance.commitShaShort} />
                    {selected && <InstanceCurrentTag />}
                </span>
            </span>
            <span className={styles.Right}>
                <InstanceStatusTag status={instance.status} />
                <InstanceUptime deployedAt={instance.deployedAt} className={contextStyles.Time} />
            </span>
        </button>
    );
};
