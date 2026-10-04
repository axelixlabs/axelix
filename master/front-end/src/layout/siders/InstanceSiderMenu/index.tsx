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
import { Menu } from "antd";
import type { AxiosResponse } from "axios";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useParams } from "react-router";

import { findOpenInstanceKeys } from "@/helpers";
import { EInstanceFeature, type IInstanceFeaturesResponse } from "@/models";
import { getInstanceFeatures } from "@/services";
import { getInstanceItems } from "@/utils";

import styles from "./styles.module.css";

export const InstanceSiderMenu = () => {
    const { t } = useTranslation();

    const { pathname } = useLocation();
    const { instanceId } = useParams();

    const [features, setFeatures] = useState<EInstanceFeature[]>([]);

    useEffect(() => {
        if (!instanceId) {
            return;
        }

        getInstanceFeatures(instanceId)
            .then((response: AxiosResponse<IInstanceFeaturesResponse>) => setFeatures(response.data.features ?? []))
            // safe fallback in case of problems
            .catch(() => {
                console.warn(
                    `The feature set of instance '${instanceId}' is not available. Performing a safe fallback`,
                );
                setFeatures([]);
            });
    }, [instanceId]);

    const items = getInstanceItems(instanceId!, t, features);

    return (
        <Menu
            mode="inline"
            items={items}
            selectedKeys={[pathname]}
            defaultOpenKeys={findOpenInstanceKeys(items, pathname)}
            className={styles.Menu}
        />
    );
};
