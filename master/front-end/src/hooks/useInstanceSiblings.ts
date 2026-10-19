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
import type { AxiosResponse } from "axios";
import { useCallback, useState } from "react";

import type { IInstanceCard, IServiceCardsResponseBody } from "@/models";
import { getInstanceSiblings } from "@/services";

/**
 * Loads the instances of the given instance's application (including the instance itself) on demand, e.g. whenever a
 * dropdown listing them opens, so they are fresh every time. The previously loaded instances stay while refreshing.
 */
export const useInstanceSiblings = (instanceId: string) => {
    const [siblings, setSiblings] = useState<IInstanceCard[]>([]);

    const load = useCallback(() => {
        getInstanceSiblings(instanceId)
            .then((response: AxiosResponse<IServiceCardsResponseBody>) => setSiblings(response.data.instances))
            .catch(() => console.warn(`The siblings of instance '${instanceId}' are not available`));
    }, [instanceId]);

    return { siblings, load };
};
