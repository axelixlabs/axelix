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
import { useEffect, useState } from "react";

import { DEMO_INSTANCES } from "@/utils/instances";

import { useInstancePreviewData } from "./useInstancePreviewData";

/** Keeps the visual action scope local to each page, independently of data refreshes. */
export const useActionInstanceSelection = (instanceId: string) => {
    const instance = useInstancePreviewData();
    const [selectedInstanceIds, setSelectedInstanceIds] = useState<string[]>([instanceId]);

    useEffect(() => {
        setSelectedInstanceIds([instanceId]);
    }, [instanceId]);

    return {
        currentInstance: instance.response,
        instances: instance.response ? [instance.response, ...DEMO_INSTANCES] : [],
        selectedInstanceIds,
        onSelectionChange: setSelectedInstanceIds,
    };
};
