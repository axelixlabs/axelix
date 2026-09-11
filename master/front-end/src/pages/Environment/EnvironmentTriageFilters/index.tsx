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

import { countTriageTag } from "@/helpers";
import { EPropertyTriageTag, type IEnvironmentPropertySource } from "@/models";

import styles from "./styles.module.css";

const TRIAGE_TAGS = [EPropertyTriageTag.DEPRECATED, EPropertyTriageTag.SUPPRESSED];

const TAG_STYLES: Record<EPropertyTriageTag, string> = {
    [EPropertyTriageTag.DEPRECATED]: styles.Deprecated,
    [EPropertyTriageTag.SUPPRESSED]: styles.Suppressed,
};

interface IProps {
    /**
     * The full, unfiltered list of property sources the counts are taken from
     */
    propertySources: IEnvironmentPropertySource[];

    /**
     * The triage tags that are currently selected
     */
    selectedTags: EPropertyTriageTag[];

    /**
     * Replaces the current selection with the given one
     */
    onSelectedTagsChange: (triageTags: EPropertyTriageTag[]) => void;
}

/**
 * The triage bar: one chip per property category that tends to need attention, each carrying the
 * number of properties in it. Clicking a chip narrows the list down to that category, so that a
 * handful of deprecated or suppressed properties do not have to be found by eye among hundreds.
 */
export const EnvironmentTriageFilters = ({ propertySources, selectedTags, onSelectedTagsChange }: IProps) => {
    const { t } = useTranslation();

    const availableTags = TRIAGE_TAGS.map((triageTag) => ({
        triageTag,
        count: countTriageTag(propertySources, triageTag),
    })).filter(({ count }) => count > 0);

    if (!availableTags.length) {
        return null;
    }

    const toggleTag = (triageTag: EPropertyTriageTag): void => {
        onSelectedTagsChange(
            selectedTags.includes(triageTag)
                ? selectedTags.filter((selectedTag) => selectedTag !== triageTag)
                : [...selectedTags, triageTag],
        );
    };

    return (
        <div className={styles.MainWrapper}>
            {availableTags.map(({ triageTag, count }) => {
                const isSelected = selectedTags.includes(triageTag);

                return (
                    <button
                        type="button"
                        className={`${styles.Chip} ${TAG_STYLES[triageTag]} ${isSelected ? styles.Selected : ""}`}
                        onClick={() => toggleTag(triageTag)}
                        aria-pressed={isSelected}
                        key={triageTag}
                    >
                        <span className={styles.Dot} />
                        {t(`Environments.triageTags.${triageTag}`)}
                        <span className={styles.Count}>{count}</span>
                    </button>
                );
            })}

            {selectedTags.length > 0 && (
                <button type="button" className={styles.Clear} onClick={() => onSelectedTagsChange([])}>
                    {t("Environments.clearFilters")}
                </button>
            )}
        </div>
    );
};
