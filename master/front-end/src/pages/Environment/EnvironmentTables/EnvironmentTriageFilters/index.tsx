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
import { PropertyTriageFilters } from "@/components";
import { countTriageTag } from "@/helpers";
import type { EPropertyTriageTag, IEnvironmentPropertySource } from "@/models";
import { environmentTriageTags } from "@/utils";

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

export const EnvironmentTriageFilters = ({ propertySources, selectedTags, onSelectedTagsChange }: IProps) => {
    const counts = environmentTriageTags.map((triageTag) => {
        return {
            triageTag: triageTag,
            count: countTriageTag(propertySources, triageTag),
        };
    });

    return (
        <PropertyTriageFilters
            counts={counts}
            selectedTags={selectedTags}
            onSelectedTagsChange={onSelectedTagsChange}
        />
    );
};
