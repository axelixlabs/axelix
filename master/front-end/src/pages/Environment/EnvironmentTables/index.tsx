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
import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";

import { PageSearch } from "@/components";
import { buildAutoCompleteOptions, buildPrecedenceIndex, filterPropertySources, getPropertiesCount } from "@/helpers";
import type { EPropertyTriageTag, IEnvironmentPropertySource } from "@/models";

import { EnvironmentModifiableTable } from "../EnvironmentModifiableTable";
import { EnvironmentProfiles } from "../EnvironmentProfiles";
import { EnvironmentTriageFilters } from "../EnvironmentTriageFilters";
import pageStyles from "../styles.module.css";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The list of property sources to render
     */
    propertySources: IEnvironmentPropertySource[];

    /**
     * The profiles the inspected application runs with
     */
    profiles: string[];
}

export const EnvironmentTables = ({ propertySources, profiles }: IProps) => {
    const { t } = useTranslation();

    const [search, setSearch] = useState<string>("");
    const [triageTags, setTriageTags] = useState<EPropertyTriageTag[]>([]);

    const isFiltered = !!search || triageTags.length > 0;
    const effectivePropertySources = isFiltered
        ? filterPropertySources(propertySources, search, triageTags)
        : propertySources;

    // Built from the full list on purpose: a chain assembled from the filtered subset would hide the
    // very sources that explain which value wins.
    const precedenceIndex = useMemo(() => buildPrecedenceIndex(propertySources), [propertySources]);

    const totalPropertiesCount = getPropertiesCount<IEnvironmentPropertySource>(propertySources);
    const filteredPropertiesCount = getPropertiesCount<IEnvironmentPropertySource>(effectivePropertySources);

    const addonAfter = `${filteredPropertiesCount} / ${totalPropertiesCount}`;

    const autocompleteOptions = buildAutoCompleteOptions(effectivePropertySources);

    return (
        <>
            <div className={styles.Toolbar}>
                <div className={styles.ToolbarRow}>
                    {profiles.length !== 0 && <EnvironmentProfiles activeProfiles={profiles} />}
                    <div className={styles.SearchSlot}>
                        <PageSearch
                            addonAfter={addonAfter}
                            setSearch={setSearch}
                            autocompleteOptions={autocompleteOptions}
                            removeBottomGutter
                        />
                    </div>
                </div>

                <EnvironmentTriageFilters
                    propertySources={propertySources}
                    selectedTags={triageTags}
                    onSelectedTagsChange={setTriageTags}
                />
            </div>

            <div className={pageStyles.Groups}>
                {effectivePropertySources.length === 0 ? (
                    <div className={styles.NoResults}>{t("Environments.noMatchingProperties")}</div>
                ) : (
                    effectivePropertySources.map((propertySource) => (
                        <EnvironmentModifiableTable
                            propertySource={propertySource}
                            precedenceIndex={precedenceIndex}
                            key={propertySource.name}
                        />
                    ))
                )}
            </div>
        </>
    );
};
