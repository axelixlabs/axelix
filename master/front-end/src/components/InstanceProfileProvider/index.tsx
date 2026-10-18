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
import { type PropsWithChildren, useEffect, useState } from "react";

import { type IInstanceProfileResponse, StatefulRequest } from "@/models";
import { getInstanceProfile } from "@/services";

import { InstanceProfileContext } from "./context";

interface IProps {
    /** The instance shared by the sidebar and all pages in this layout */
    instanceId: string;
}

/**
 * Loads the profile of the instance once per instance view and shares it with the sidebar and the pages: the instance
 * itself, the number of its siblings and the features it has. The siblings themselves are loaded on demand.
 */
export const InstanceProfileProvider = ({ instanceId, children }: PropsWithChildren<IProps>) => {
    const [profile, setProfile] = useState(StatefulRequest.loading<IInstanceProfileResponse>());

    // TODO: Discuss instance lifecycle and refresh behavior before implementing live updates.
    // The profile is currently loaded once per layout instance. Instances may appear, disappear,
    // or change status while the page is open. Agree on the refresh strategy, how to reconcile
    // each page's selection when instances disappear, and what to do if the current instance
    // disappears. Keep the existing data visible while refreshing.
    useEffect(() => {
        let active = true;

        getInstanceProfile(instanceId)
            .then((response: AxiosResponse<IInstanceProfileResponse>) => {
                if (active) {
                    setProfile(StatefulRequest.success(response.data));
                }
            })
            .catch(() => {
                if (active) {
                    setProfile(StatefulRequest.error(""));
                }
            });

        return () => {
            active = false;
        };
    }, [instanceId]);

    return <InstanceProfileContext.Provider value={profile}>{children}</InstanceProfileContext.Provider>;
};
