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

    // Loaded once on purpose: the siblings, which do change while the page is open, are refetched whenever a dropdown
    // listing them opens. If the instance itself goes away, the requests of the page fail with INSTANCE_NOT_FOUND,
    // which already points the user back to the Wallboard.
    useEffect(() => {
        getInstanceProfile(instanceId)
            .then((response: AxiosResponse<IInstanceProfileResponse>) =>
                setProfile(StatefulRequest.success(response.data)),
            )
            .catch(() => setProfile(StatefulRequest.error("")));
    }, [instanceId]);

    return <InstanceProfileContext.Provider value={profile}>{children}</InstanceProfileContext.Provider>;
};
