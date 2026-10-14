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
import { type PropsWithChildren, useEffect, useRef, useState } from "react";

import { type IInstanceCard, type IServiceCardsResponseBody, StatefulRequest } from "@/models";
import { getWallboardData } from "@/services";

import { InstancePreviewContext } from "./context";

interface IProps {
    /** The instance shared by the sidebar and all pages in this layout */
    instanceId: string;
}

export const InstancePreviewProvider = ({ instanceId, children }: PropsWithChildren<IProps>) => {
    const [instance, setInstance] = useState(StatefulRequest.loading<IInstanceCard | null>());
    const requestRef = useRef<Promise<AxiosResponse<IServiceCardsResponseBody>> | null>(null);

    // TODO: Discuss instance lifecycle and refresh behavior before implementing live updates.
    // Metadata is currently loaded once per layout instance. Instances may appear, disappear,
    // or change status while the page is open. Agree on the refresh strategy, how to reconcile
    // each page's selection when instances disappear, and what to do if the current instance
    // disappears. Keep the existing data visible while refreshing.
    useEffect(() => {
        let active = true;
        const isCurrentInstance = (item: IInstanceCard): boolean => item.instanceId === instanceId;

        // Reuse the request when StrictMode re-runs the effect during development.
        requestRef.current ??= getWallboardData();
        requestRef.current
            .then((response) => {
                if (active) {
                    setInstance(StatefulRequest.success(response.data.instances.find(isCurrentInstance) ?? null));
                }
            })
            .catch(() => {
                if (active) {
                    setInstance(StatefulRequest.error(""));
                }
            });

        return () => {
            active = false;
        };
    }, [instanceId]);

    return <InstancePreviewContext.Provider value={instance}>{children}</InstancePreviewContext.Provider>;
};
